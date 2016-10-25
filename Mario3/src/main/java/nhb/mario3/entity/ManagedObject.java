package nhb.mario3.entity;

import nhb.common.Loggable;
import nhb.common.data.PuObject;

public interface ManagedObject extends LifeCycle, Pluggable, Loggable {

	Object acquire(PuObject requestParams);

	void release(Object object);
}
