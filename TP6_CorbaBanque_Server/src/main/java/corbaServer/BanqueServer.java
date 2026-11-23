package corbaServer;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import service.BanqueImpl;

import javax.naming.Context;
import javax.naming.InitialContext;

public class BanqueServer {

    public static void main(String[] args) {
        try {
            // a. Initialiser l'ORB
            ORB orb = ORB.init(args, null);

            // b. Initialiser l'adaptateur d'objets
            POA rootPOA = POAHelper.narrow(
                    orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // c. Configurer l'annuaire JNDI
            Context ctx = new InitialContext();

            // d. Créer le servant
            BanqueImpl banque = new BanqueImpl();

            // e. Enregistrer la référence CORBA
            org.omg.CORBA.Object ref =
                    rootPOA.servant_to_reference(banque);
            ctx.rebind("banque", ref);

            System.out.println(
                    "Objet 'banque' enregistré dans l'annuaire : "
                            + orb.object_to_string(ref));

            // f. Attendre les requêtes clients
            System.out.println(
                    "Serveur CORBA Banque démarré, en attente des clients...");
            orb.run();

        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
