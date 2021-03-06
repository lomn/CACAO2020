package abstraction.eq7Distributeur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstraction.eq8Romu.produits.Chocolat;
import abstraction.eq8Romu.produits.ChocolatDeMarque;
import abstraction.fourni.Filiere;
import abstraction.fourni.IActeur;
import abstraction.fourni.Journal;
import abstraction.fourni.Variable;

public class AbsAcheteurBourse {
	
	// Journal principal
	protected Journal journal;
	
	// Stockage des quantités commandées à chaque étape
	protected Map<Chocolat, Variable> quantitesACommander;
	
	// Stockage de la commande impayée si elle existe
	protected Pair<Chocolat, Double> commandeImpayee;

	// Référence à l'acteur principal
	protected Distributeur2 ac;
	
	// Couleurs d'arrière-plan pour les messages des journaux
	public Color titleColor = Color.BLACK;
	public Color alertColor = Color.RED;
	public Color warningColor = Color.ORANGE;
	public Color positiveColor = Color.GREEN;
	public Color descriptionColor = Color.YELLOW;
	
	public AbsAcheteurBourse(Distributeur2 ac) {
		this.ac = ac;	
		initJournaux();
		quantitesACommander = new HashMap<Chocolat, Variable>();
		commandeImpayee = null;
		for (Chocolat choco : Chocolat.values()) {
			quantitesACommander.put(choco, new Variable(getNom() + " : " + choco.name() + " [Demande i-1]", ac, 0));
		}
	}
	
	// Initialise les journaux
	public void initJournaux() {
		this.journal = new Journal(this.getNom() + " : Acheteur Chocolat Bourse", ac);
		journal.ajouter(Journal.texteColore(titleColor, Color.WHITE, this.getNom() + " : Acheteur Bourse"));
		journal.ajouter(Journal.texteColore(descriptionColor, Color.BLACK, "Ce journal suit les activités de l'acheteur de chocolat à la bourse"));
		journal.ajouter(Journal.texteColore(descriptionColor, Color.BLACK, "associé à ce distributeur. Il affiche les commandes effectuées et leur"));
		journal.ajouter(Journal.texteColore(descriptionColor, Color.BLACK, "statut (payée, impayée) ainsi que les réceptions de commandes."));
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		return res;
	}

	// Renvoie les paramètres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	public List<Journal> getJournaux() {
		List<Journal> res = new ArrayList<Journal>();
		res.add(journal);
		return res;
	}
	
	// Méthodes renvoyant aux méthodes de l'acteur principal
	public String getNom() {
		return ac.getNom();
	}
	
	public String getDescription() {
		return ac.getDescription();
	}

	public Color getColor() {
		return ac.getColor();
	}

	public List<String> getNomsFilieresProposees() {
		return ac.getNomsFilieresProposees();
	}

	public Filiere getFiliere(String nom) {
		return ac.getFiliere(nom);
	}

	public void setCryptogramme(Integer crypto) {
		ac.setCryptogramme(crypto);
	}

	public void notificationFaillite(IActeur acteur) {
		ac.notificationFaillite(acteur);
	}

	public void notificationOperationBancaire(double montant) {
		ac.notificationOperationBancaire(montant);
	}
	
} 
