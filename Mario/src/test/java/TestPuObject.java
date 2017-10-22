import com.nhb.common.data.PuObject;

public class TestPuObject {

	public static void main(String[] args) {
		PuObject puo = new PuObject();
		puo.set("name", "bachden");

		System.out.println(puo.toJSON());
	}
}
