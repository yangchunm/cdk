package org.openscience.cdk.smiles;

import com.google.common.collect.Maps;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.annotations.TestClass;
import org.openscience.cdk.annotations.TestMethod;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomParity;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import uk.ac.ebi.beam.Atom;
import uk.ac.ebi.beam.AtomBuilder;
import uk.ac.ebi.beam.Bond;
import uk.ac.ebi.beam.Graph;
import uk.ac.ebi.beam.Configuration;
import uk.ac.ebi.beam.Edge;
import uk.ac.ebi.beam.GraphBuilder;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openscience.cdk.interfaces.IDoubleBondStereochemistry.Conformation.TOGETHER;
import static org.openscience.cdk.interfaces.ITetrahedralChirality.Stereo.CLOCKWISE;

/**
 * Convert a CDK {@link IAtomContainer} to a Beam graph object for generating
 * SMILES. Once converted the Beam ChemicalGraph can be manipulated further to
 * generate a standard-from SMILES and/or arrange the vertices in a canonical
 * output order.
 *
 * <b>Important:</b> The conversion respects the implicit hydrogen count and if
 * the number of implicit hydrogen ({@link IAtom#getImplicitHydrogenCount()}) is
 * null an exception will be thrown. To ensure correct conversion please ensure
 * all atoms have their implicit hydrogen count set.
 *
 * <blockquote><pre>
 * IAtomContainer m   = ...;
 *
 * // converter is thread-safe and can be used by multiple threads
 * CDKToBeam      c2g = new CDKToBeam();
 * ChemicalGraph  g   = c2g.toBeamGraph(m);
 *
 * // get the SMILES notation from the Beam graph
 * String         smi = g.toSmiles():
 * </pre></blockquote>
 *
 * @author John May
 * @cdk.module smiles
 * @cdk.keyword SMILES
 * @see <a href="http://johnmay.github.io/Beam">Beam SMILES Toolkit</a>
 */
@TestClass("org.openscience.cdk.smiles.CDKToBeamTest")
final class CDKToBeam {

    /**
     * Whether to convert the molecule with isotope and stereo information -
     * Isomeric SMILES.
     */
    private final boolean isomeric;

    /** Create a isomeric converter. */
    CDKToBeam() {
        this(true);
    }

    /** Create a converter specifying whether to be isomeric or not. */
    CDKToBeam(boolean isomeric) {
        this.isomeric = isomeric;
    }

    /**
     * Convert a CDK {@link IAtomContainer} to a Beam ChemicalGraph. The graph
     * can when be written directly as to a SMILES or manipulated further (e.g
     * canonical ordering/standard-form and other normalisations).
     *
     * @param ac an atom container instance
     * @return the Beam ChemicalGraph for additional manipulation
     */
    @TestMethod("adenine,benzene,imidazole")
    Graph toBeamGraph(IAtomContainer ac) {

        int order = ac.getAtomCount();

        GraphBuilder        gb      = GraphBuilder.create(order);
        Map<IAtom, Integer> indices = Maps.newHashMapWithExpectedSize(order);

        for (IAtom a : ac.atoms()) {
            indices.put(a, indices.size());
            gb.add(toBeamAtom(a));
        }

        for (IBond b : ac.bonds()) {
            gb.add(toBeamEdge(b, indices));
        }

        // configure stereo-chemistry by encoding the stereo-elements
        if (isomeric) {
            for (IStereoElement se : ac.stereoElements()) {
                if (se instanceof ITetrahedralChirality) {
                    addTetrahedralConfiguration((ITetrahedralChirality) se, gb, indices);
                } else if (se instanceof IDoubleBondStereochemistry) {
                    addGeometricConfiguration((IDoubleBondStereochemistry) se, gb, indices);
                } else if (se instanceof IAtomParity) {
                    addAtomParityConfiguration((IAtomParity) se, gb, indices);
                }
            }
        }

        return gb.build();
    }

    /**
     * Convert an CDK {@link IAtom} to a Beam Atom. The symbol and implicit
     * hydrogen count are not optional. If the symbol is not supported by the
     * SMILES notation (e.g. 'R1') the element will automatically default to
     * UNKNOWN ('*').
     *
     * @param a cdk Atom instance
     * @return a Beam atom
     * @throws NullPointerException the atom had an undefined symbol or implicit
     *                              hydrogen count
     */
    @TestMethod("aliphaticAtom,aromaticAtom") Atom toBeamAtom(final IAtom a) {

        final boolean aromatic = a.getFlag(CDKConstants.ISAROMATIC);
        final Integer hCount   = checkNotNull(a.getImplicitHydrogenCount(),
                                              "An atom had an undefined number of implicit hydrogens");
        final Integer charge   = a.getFormalCharge();
        final String  symbol   = checkNotNull(a.getSymbol(),
                                              "An atom had an undefined symbol");

        AtomBuilder ab = aromatic ? AtomBuilder.aromatic(symbol)
                                               .hydrogens(hCount)
                                  : AtomBuilder.aliphatic(symbol)
                                               .hydrogens(hCount);

        if (charge != null)
            ab.charge(charge);

        // use the mass number to specify isotope?
        if (isomeric) {
            Integer massNumber = a.getMassNumber();
            if (massNumber != null)
                ab.isotope(massNumber);
        }

        // could also add atom class from property (overhead of ChemObject
        // HashMap) or perhaps passed atom classes as an array of 'int[]'.
        // ab.atomClass(a.getProperty("smi:AtomClass")); ?

        return ab.build();
    }

    /**
     * Convert a CDK {@link IBond} to a Beam edge.
     *
     * @param b       the CDK bond
     * @param indices map of atom indices
     * @return a Beam edge
     * @throws IllegalArgumentException the bond did not have 2 atoms or an
     *                                  unsupported order
     * @throws NullPointerException     the bond order was undefined
     */
    @TestMethod("singleBond,doubleBond,tripleBond")
    Edge toBeamEdge(IBond b, Map<IAtom, Integer> indices) {

        checkArgument(b.getAtomCount() == 2,
                      "Invalid number of atoms on bond");

        int u = indices.get(b.getAtom(0));
        int v = indices.get(b.getAtom(1));

        return toBeamEdgeLabel(b).edge(u, v);
    }

    /**
     * Convert a CDK {@link IBond} to the Beam edge label type.
     *
     * @param b cdk bond
     * @return the edge label for the Beam edge
     * @throws NullPointerException     the bond order was null and the bond was
     *                                  not-aromatic
     * @throws IllegalArgumentException the bond order could not be converted
     */
    private Bond toBeamEdgeLabel(IBond b) {

        if (b.getFlag(CDKConstants.ISAROMATIC))
            return Bond.AROMATIC;

        IBond.Order order = checkNotNull(b.getOrder(),
                                         "A bond had undefined order");

        switch (order) {
            case SINGLE:
                return Bond.SINGLE;
            case DOUBLE:
                return Bond.DOUBLE;
            case TRIPLE:
                return Bond.TRIPLE;
            case QUADRUPLE:
                return Bond.QUADRUPLE;
            default:
                throw new IllegalArgumentException("Unsupported bond order: " + order);
        }
    }

    /**
     * Add double-bond stereo configuration to the Beam GraphBuilder.
     *
     * @param dbs     stereo element specifying double-bond configuration
     * @param gb      the current graph builder
     * @param indices atom indices
     */
    private void addGeometricConfiguration(IDoubleBondStereochemistry dbs,
                                           GraphBuilder gb,
                                           Map<IAtom, Integer> indices) {

        IBond   db = dbs.getStereoBond();
        IBond[] bs = dbs.getBonds();

        int u = indices.get(db.getAtom(0));
        int v = indices.get(db.getAtom(1));

        // is bs[0] always connected to db.atom(0)?
        int x = indices.get(bs[0].getConnectedAtom(db.getAtom(0)));
        int y = indices.get(bs[1].getConnectedAtom(db.getAtom(1)));

        if (dbs.getStereo() == TOGETHER) {
            gb.geometric(u, v).together(x, y);
        } else {
            gb.geometric(u, v).opposite(x, y);
        }
    }

    /**
     * Add tetrahedral stereo configuration to the Beam GraphBuilder.
     *
     * @param tc      stereo element specifying tetrahedral configuration
     * @param gb      the current graph builder
     * @param indices atom indices
     */
    private void addTetrahedralConfiguration(ITetrahedralChirality tc,
                                             GraphBuilder gb,
                                             Map<IAtom, Integer> indices) {

        IAtom[] ligands = tc.getLigands();

        int u    = indices.get(tc.getChiralAtom());
        int vs[] = new int[]{
                indices.get(ligands[0]),
                indices.get(ligands[1]),
                indices.get(ligands[2]),
                indices.get(ligands[3])
        };

        gb.tetrahedral(u)
          .lookingFrom(vs[0])
          .neighbors(vs[1], vs[2], vs[3])
          .winding(tc.getStereo() == CLOCKWISE ? Configuration.CLOCKWISE
                                               : Configuration.ANTI_CLOCKWISE)
          .build();
    }

    /**
     * Add atom parity (tetrahedral) stereo configuration to the Beam
     * GraphBuilder.
     *
     * @param ap      stereo element specifying atom parity configuration
     * @param gb      the current graph builder
     * @param indices atom indices
     */
    private void addAtomParityConfiguration(IAtomParity ap,
                                            GraphBuilder gb,
                                            Map<IAtom, Integer> indices) {

        IAtom[] ligands = ap.getSurroundingAtoms();

        int u    = indices.get(ap.getAtom());
        int vs[] = new int[]{
                indices.get(ligands[0]),
                indices.get(ligands[1]),
                indices.get(ligands[2]),
                indices.get(ligands[3])
        };

        gb.tetrahedral(u)
          .lookingFrom(vs[0])
          .neighbors(vs[1], vs[2], vs[3])
          .parity(ap.getParity())
          .build();
    }
}
