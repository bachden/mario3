package nhb.mario3.cache.hazelcast;

import com.hazelcast.config.Config;

/**
 * this interface will be removed in near future please use "initializers" in
 * extension.xml instead
 * 
 * @author bachden
 *
 */
@Deprecated
public interface HazelcastInitializer {

	void prepare(Config config);
}
