public class StatusValue {

	public enum Values { 
		LO("L"),
		HI("H");
		
		private String label;
		
		Values(final String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return this.label;
		}
	}
}

