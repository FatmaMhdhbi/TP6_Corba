package corbaClient;

import corbaBanque.*;
import javax.naming.Context;
import javax.naming.InitialContext;

public class BanqueClient {

    public static void main(String[] args) {
        try {
            // Connexion à l'annuaire JNDI
            Context ctx = new InitialContext();

            // Récupération de la référence distante
            org.omg.CORBA.Object ref =
                    (org.omg.CORBA.Object) ctx.lookup("banque");

            // Conversion vers l'interface distante
            IBanqueRemote banque = IBanqueRemoteHelper.narrow(ref);

            // 1. Créer un compte
            Compte cpte = new Compte();
            cpte.code = 1;
            cpte.solde = 1000;

            banque.creerCompte(cpte);
            System.out.println("Compte créé.");

            // 2. Effectuer un versement
            banque.verser(200, 1);
            System.out.println("Versement effectué.");

            // 3. Effectuer un retrait
            banque.retirer(100, 1);
            System.out.println("Retrait effectué.");

            // 4. Consulter un compte
            Compte c = banque.getCompte(1);
            System.out.println("Compte : " + c.code);
            System.out.println("Solde : " + c.solde);

            // 5. Consulter tous les comptes
            Compte[] comptes = banque.getComptes();

            for (Compte compte : comptes) {
                System.out.println("Code : " + compte.code
                        + " | Solde : " + compte.solde);
            }

            // 6. Conversion Euro vers Dinar Tunisien
            double resultat = banque.conversion(100);
            System.out.println("100 Euros = " + resultat + " DT");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
