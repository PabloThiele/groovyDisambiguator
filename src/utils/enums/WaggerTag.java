package utils.enums;

public enum WaggerTag {
	
	ADJ 		("Adjetivo"),
	SUBS		("Substantivo"),	
	ART			("Artigo"),
	PREP 		("Preposição"),
	CONJ		("Conjunção"),
	NUM 		("Numeral"),
	PRO 		("Pronome"),
	V 			("Verbo"),
	ADV 		("Advérbio"),
	PREF 		("Prefixo"),
	SIG 		("Sigla"),
	ABR 		("Abreviatura"),
	INT			("Interjeição"),
	PREPART 	("Preposição+Artigo"),
	PREP_PRO 	("Preposição+Pronome"),
	PREP_ADV 	("Preposição+Advérbio"),
	PRO_PRO 	("Pronome+Pronome"),
	PRE_PRE 	("Preposição+Preposição"),
	UNKNOWN		("?");
	
	private String type;
	
	private WaggerTag(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	public static WaggerTag getTagByType(String type){
		WaggerTag returnedTag = WaggerTag.UNKNOWN;
		for (WaggerTag tag : WaggerTag.values()) {
			if(type.equals(tag.getType())){
				returnedTag = tag;
			}
		}
		return returnedTag;
	}


}
