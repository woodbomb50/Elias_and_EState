import java.util.ArrayList;

public class StatusArray implements IDiffuseStatus {

	private ArrayList<DiffuseStatusCharacteristic> list = new ArrayList<DiffuseStatusCharacteristic>();

        /*
         * Default constructor used when actual user data is to be processed.
         * A subset of the available diffuse status characteristics is added,
         * one characteristic at a time.
         **/
	public StatusArray() {
	}

        /*
         * Alternate constructor used when random user data is to be generated
         * for diffuse status characteristics.
         **/
        public void generateRandomStatusArray() {

		add(new DiffuseStatusCharacteristic(ScType.GENDER, minIndex));
                add(new DiffuseStatusCharacteristic(ScType.EDUCATION, minIndex));
		add(new DiffuseStatusCharacteristic(ScType.AGE, minIndex));
		add(new DiffuseStatusCharacteristic(ScType.RACE, minIndex));
                add(new DiffuseStatusCharacteristic(ScType.LANGUAGE, minIndex));
                add(new DiffuseStatusCharacteristic(ScType.OCCUPATION, minIndex));
	}


        /**
         * Add a single initialized diffuse status characteristic to the list.
         * @param dsc
         */
        public void add(DiffuseStatusCharacteristic dsc) {
		list.add(dsc);
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(DiffuseStatusCharacteristic dsc) {
		return list.contains(dsc);
	}

	public void del(DiffuseStatusCharacteristic dsc) {

		int index =  list.indexOf(dsc);
		if (-1 < index) {
			list.remove(index);
		}
	}
	
	public ArrayList<DiffuseStatusCharacteristic> getList() {
		return list;
	}

	public DiffuseStatusCharacteristic get(ScType type) {

		DiffuseStatusCharacteristic dsc = null;

		for (int i = 0; i < list.size(); i++) {
			dsc = list.get(i);
			if (type == dsc.getScType()) {
				break;
			}
		}
		return dsc;
	}

	public int getSize() {
		return list.size();
	}
}
