package abstraction.eq4Transformateur2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import abstraction.eq8Romu.contratsCadres.ExemplaireContratCadre;
import abstraction.eq8Romu.contratsCadres.FiliereTestContratCadre;
import abstraction.eq8Romu.produits.Chocolat;
import abstraction.eq8Romu.produits.Feve;
import abstraction.fourni.Filiere;
import abstraction.fourni.IActeur;

public class Transformateur2 extends Transformateur2_negoce {
	private int tourAvecCapaEnTrop;

	public Transformateur2 () {
		super();
		this.tourAvecCapaEnTrop = 0;
	}

	public int getTourAvecCapaEnTrop() {
		return tourAvecCapaEnTrop;
	}

	public void setTourAvecCapaEnTrop(int tourAvecCapaEnTrop) {
		this.tourAvecCapaEnTrop = tourAvecCapaEnTrop;
	}
	
	public List<String> getNomsFilieresProposees() {
		ArrayList<String> liste = new ArrayList<String>();
		liste.add("TestFiliereTransformateur2") ;
		return liste ;
	}

	public Filiere getFiliere(String nom) {
		switch (nom) { 
		case "TestFiliereTransformateur2" : return new TestFiliereTransformateur2() ;
	    default : return null;
		}
	}

	public void next() {
		List<ExemplaireContratCadre> contratsObsoletes = new LinkedList<ExemplaireContratCadre>();
		for (ExemplaireContratCadre contrat : this.mesContratEnTantQueVendeur) {
			if (contrat.getQuantiteRestantALivrer() == 0.0 && contrat.getMontantRestantARegler() == 0.0) {
				contratsObsoletes.add(contrat);
			}
		}
		this.mesContratEnTantQueVendeur.removeAll(contratsObsoletes);
		super.majQuantitePateCC();
		
		double montant = super.coutStocks() ; //coût des stocks de l'année précédente

		// Transformer selon priorités de production
		montant += this.transformerSelonPriorites();

		// Gestion des priorites de prod
		
		// Coûts de gestion
		montant += super.getCoutFixeValeur() ;
		Filiere.LA_FILIERE.getBanque().virer(this, super.cryptogramme, Filiere.LA_FILIERE.getBanque(), montant) ;
		
		// Investissements
		/*double solde = super.getSolde();
		if (solde >= 0) {
			double investTFEP = solde * INVESTI_MOYPROD;
			super.investirCapaTFEP(investTFEP);
			this.investissementChoco();
		}*/
	}

	public double transformerSelonPriorites() {
		
		Map<Feve, Double> FevesATransfo = new HashMap<Feve, Double>();
		// PATE POUR CC EN PREMIER
		int tourAutoBasse = super.nbToursAutonomiePate(PateInterne.PATE_BASSE);
		int tourAutoMoy = super.nbToursAutonomiePate(PateInterne.PATE_MOYENNE);
		if (tourAutoBasse < super.getNombreDeTourDautoMin()) {
			Double pateBasseManquante = super.getQuantiteALivrerPourITour(PateInterne.PATE_BASSE,
					super.getNombreDeTourDautoMin()) - super.getStockPateValeur(PateInterne.PATE_BASSE);
			if (tourAutoMoy < super.getNombreDeTourDautoMin()) {
				Double pateMoyManquante = super.getQuantiteALivrerPourITour(PateInterne.PATE_MOYENNE,
						super.getNombreDeTourDautoMin()) - -super.getStockPateValeur(PateInterne.PATE_MOYENNE);
				if (pateBasseManquante < pateMoyManquante) {
					FevesATransfo.put(Feve.FEVE_BASSE, pateBasseManquante / super.getCoeffTFEP());
					FevesATransfo.put(Feve.FEVE_MOYENNE, pateMoyManquante / super.getCoeffTFEP());
				} else {
					FevesATransfo.put(Feve.FEVE_MOYENNE, pateMoyManquante / super.getCoeffTFEP());
					FevesATransfo.put(Feve.FEVE_BASSE, pateBasseManquante / super.getCoeffTFEP());
				}
			} else {
				FevesATransfo.put(Feve.FEVE_BASSE, pateBasseManquante / super.getCoeffTFEP());
			}
		} else if (tourAutoMoy < super.getNombreDeTourDautoMin()) {
			Double pateMoyManquante = super.getQuantiteALivrerPourITour(PateInterne.PATE_MOYENNE,
					super.getNombreDeTourDautoMin()) - -super.getStockPateValeur(PateInterne.PATE_MOYENNE);
			FevesATransfo.put(Feve.FEVE_MOYENNE, pateMoyManquante / super.getCoeffTFEP());
		}
		// LISTE DE PATE POUR CHOCO
		Map<PateInterne, Double> PateATransfo = new HashMap<PateInterne, Double>();
		List<Chocolat> chocos = new LinkedList<Chocolat>();
		chocos.add(Chocolat.CHOCOLAT_MOYENNE_EQUITABLE);
		chocos.add(Chocolat.CHOCOLAT_HAUTE_EQUITABLE);
		chocos.add(Chocolat.CHOCOLAT_HAUTE);
		for (Chocolat choco : chocos) {
			PateInterne pate = super.creerPateAPartirDeChocolat(choco);
			PateATransfo.put(pate, super.getStockPateValeur(pate));
		
		}
		// FEVE POUR CHOCO
		List<PateInterne> patesRestantes = new LinkedList<PateInterne>();
		patesRestantes.add(PateInterne.PATE_MOYENNE_EQUITABLE);
		patesRestantes.add(PateInterne.PATE_HAUTE_EQUITABLE);
		patesRestantes.add(PateInterne.PATE_HAUTE);
		for (PateInterne pate : patesRestantes) {
			Feve feve = super.creerFeve(pate);
			FevesATransfo.put(feve, super.getStockFevesValeur(feve));
		}

		// TRANSFORMATION EFFECTIVE
		double cout = 0 ;
		this.correctionQuantitesTFEP(FevesATransfo) ; // limite avec les stocks disponibles
		this.correctionQuantitesTPEC(PateATransfo) ;
		cout += super.transformationPate(PateATransfo);
		cout += super.transformationFeves(FevesATransfo);
		return cout ;
	}

	public void investissementChoco() {
		double capaPateEnPlus = super.getCapaciteMaxTFEP() - super.getQuantitePateCCTotaleValeur();
		if (capaPateEnPlus > 0) {
			double capaNecessaire = capaPateEnPlus - super.getCapaciteMaxTPEC();
			double rapport = capaNecessaire / capaPateEnPlus;
			if (rapport < 0) {
				boolean noStockPateEnPlus = super.getStockPateValeur(PateInterne.PATE_MOYENNE_EQUITABLE)
						+ super.getStockPateValeur(PateInterne.PATE_HAUTE_EQUITABLE)
						+ super.getStockPateValeur(PateInterne.PATE_HAUTE) == 0;
				if (noStockPateEnPlus) {
					this.setTourAvecCapaEnTrop(this.getTourAvecCapaEnTrop() + 1);

					if (this.getTourAvecCapaEnTrop() > 3) {
						super.setCapaciteMaxTPEC(capaPateEnPlus);
						this.setTourAvecCapaEnTrop(0);
					}
				}
			} else if (rapport > 0.1) {
				double solde = super.getSolde();
				double qteAInvest = super.getCoutPourAugmenterCapaTPEC() * capaNecessaire;
				if (qteAInvest > 0.1 * solde) {
					super.investirCapaTPEC(0.1 * solde);
				} else {
					super.investirCapaTPEC(qteAInvest);
				}
			}
		}
	}
	
	public void correctionTFP (HashMap<Feve,Double> quantitesFeves) {
		for (Feve feve : Feve.values()) {
			if (quantitesFeves.get(feve) > super.getStockFevesValeur(feve)) {
				quantitesFeves.replace(feve, super.getStockFevesValeur(feve)) ;
			}
		}
	}
	
	public void correctionTPEC (HashMap<PateInterne,Double> quantitesPates) {
		for (PateInterne pate : PateInterne.values()) {
			if (quantitesPates.get(pate) > super.getStockPateValeur(pate)) {
				quantitesPates.replace(pate, super.getStockPateValeur(pate)) ;
			}
		}
	}
}
