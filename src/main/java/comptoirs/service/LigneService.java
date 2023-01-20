package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Service
@Validated // Les contraintes de validatipn des méthodes sont vérifiées
public class LigneService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final LigneRepository ligneDao;
    private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public LigneService(CommandeRepository commandeDao, LigneRepository ligneDao, ProduitRepository produitDao) {
        this.commandeDao = commandeDao;
        this.ligneDao = ligneDao;
        this.produitDao = produitDao;
    }

    /**
     * <pre>
     * Service métier :
     *     Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     *     Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
     * Règles métier :
     *     - le produit référencé doit exister
     *     - la commande doit exister
     *     - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     *     - la quantité doit être positive
     *     - On doit avoir une quantite en stock du produit suffisante
     * <pre>
     *
     *  @param commandeNum la clé de la commande
     *  @param produitRef la clé du produit
     *  @param quantite la quantité commandée (positive)
     */
    @Transactional
    Ligne ajouterLigne(Integer commandeNum, Integer produitRef, @Positive int quantite) {
        // vérifier que le produit existe
        var produit = produitDao.findById(produitRef).orElseThrow();
        // vérifier que la commande existe
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        // vérifier que la commande ne soit pas déjà envoyé
        if (commande.getEnvoyeele() == null) { // on met à jour la date d'expédition avec la date du jour
            commande.setEnvoyeele(LocalDate.now());
            if (quantite < 0) { // vérifier que la quantité est positive
                throw new UnsupportedOperationException("La quantité doit être positive");
            } else { // vérifier qu'on a une quantite en stock du produit suffisante
                if (quantite > produit.getUnitesEnStock()) {
                    throw new UnsupportedOperationException("Stock insuffisant par rapport à la quantité demandé");
                } else { // décrémenter la quantité en stock de la quantité commandée
                    produit.setUnitesEnStock(produit.getUnitesEnStock() - quantite);
                } //créer une nouvelle ligne
                var nouvelleLigne = new Ligne(commande, produit, quantite);
                // incrémenter la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
                produit.setUnitesCommandees(produit.getUnitesCommandees() + nouvelleLigne.getQuantite());
                commande.getLignes().add(nouvelleLigne);
                return nouvelleLigne;
            }
            } else{
                throw new UnsupportedOperationException("La commande a déjà été envoyée.");
            }
        }
    }

