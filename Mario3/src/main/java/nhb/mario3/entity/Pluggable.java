package nhb.mario3.entity;

import nhb.mario3.api.MarioApi;

public interface Pluggable {

	void setApi(MarioApi api);

	MarioApi getApi();
}
