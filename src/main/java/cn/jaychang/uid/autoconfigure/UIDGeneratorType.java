package cn.jaychang.uid.autoconfigure;

/**
 * @Auther: fsren
 *
 * @Date: 2018/10/23 14:37
 *
 * @Description:
 *  <p>
 *
 *  </p>
 */
public enum UIDGeneratorType {

	/**
	 * 默认ID生成方式
	 */
	DEFAULT(1),


	/**
	 * 如对UID生成性能有要求, 请使用CACHED
	 */
	CACHED(2);

	private int value;

	UIDGeneratorType(int value) {
		this.value = value;
	}

	public static UIDGeneratorType valueOf(Integer value) {
		if (value == null) {
			return null;
		}
		for (UIDGeneratorType e : UIDGeneratorType.values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		return null;
	}

	public int getValue() {
		return this.value;
	}


}
