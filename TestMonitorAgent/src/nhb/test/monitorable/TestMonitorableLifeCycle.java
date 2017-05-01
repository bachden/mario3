package nhb.test.monitorable;

import com.mario.entity.impl.BaseLifeCycle;
import com.mario.monitor.Monitorable;
import com.mario.monitor.MonitorableResponse;
import com.mario.monitor.MonitorableStatus;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;

public class TestMonitorableLifeCycle extends BaseLifeCycle implements Monitorable {

	@Override
	public void initMonitoring(PuObjectRO params) {
		getLogger().debug("init monitoring with params: " + params);
	}

	@Override
	public MonitorableResponse checkStatus() {
		return new MonitorableResponse() {

			@Override
			public MonitorableStatus getStatus() {
				return MonitorableStatus.CRITICAL;
			}

			@Override
			public String getMessage() {
				return "Critical status testing...";
			}

			@Override
			public PuElement getDetails() {
				return null;
			}
		};
	}

}
