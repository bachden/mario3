package com.mario.entity.message.data;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.mario.exceptions.OperationNotSupported;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.PrimitiveTypeUtils;

import net.minidev.json.JSONObject;

public class PuHttpRequestObject implements PuObjectRO {

	private static final long serialVersionUID = -4529934847830679379L;

	private HttpServletRequest request;

	public PuHttpRequestObject() {
		// do nothing
	}

	public PuHttpRequestObject(HttpServletRequest request) {
		this();
		this.request = request;
	}

	@Override
	public byte[] toBytes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toJSON() {
		return this.toMap().toString();
	}

	@Override
	public String toXML() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeTo(OutputStream out) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return this.request.getParameterMap().size();
	}

	@Override
	public Map<String, ?> toMap() {
		if (this.request == null) {
			return null;
		}
		Map<String, Object> map = new JSONObject();
		Enumeration<String> it = this.request.getParameterNames();
		while (it.hasMoreElements()) {
			String key = it.nextElement();
			map.put(key, this.getString(key));
		}
		return map;
	}

	@Override
	public Iterator<Entry<String, PuValue>> iterator() {
		throw new UnsupportedOperationException("Iterator do not supported in PuHttpRequestObject");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String fieldName) {
		return (T) this.getString(fieldName);
	}

	@Override
	public boolean variableExists(String fieldName) {
		return this.request.getParameterMap().containsKey(fieldName);
	}

	@Override
	public PuObject deepClone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PuDataType typeOf(String field) {
		return PuDataType.STRING;
	}

	@Override
	public boolean getBoolean(String fieldName) {
		return PrimitiveTypeUtils.getBooleanValueFrom(this.getString(fieldName));
	}

	@Override
	public boolean getBoolean(String fieldName, boolean defaultValue) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getBooleanValueFrom(fieldName);
		}
		return defaultValue;
	}

	@Override
	public byte[] getRaw(String fieldName) {
		if (this.variableExists(fieldName)) {
			return this.getString(fieldName).getBytes();
		}
		return null;
	}

	@Override
	public byte getByte(String fieldName) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getByteValueFrom(this.getString(fieldName));
		}
		return 0;
	}

	@Override
	public byte getByte(String fieldName, byte defaultValue) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getByteValueFrom(this.getString(fieldName));
		}
		return defaultValue;
	}

	@Override
	public short getShort(String fieldName) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getShortValueFrom(this.getString(fieldName));
		}
		return 0;
	}

	@Override
	public short getShort(String fieldName, short defaultValue) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getShortValueFrom(this.getString(fieldName));
		}
		return defaultValue;
	}

	@Override
	public int getInteger(String fieldName) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getIntegerValueFrom(this.getString(fieldName));
		}
		return 0;
	}

	@Override
	public int getInteger(String fieldName, int defaultValue) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getIntegerValueFrom(this.getString(fieldName));
		}
		return defaultValue;
	}

	@Override
	public float getFloat(String fieldName) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getFloatValueFrom(this.getString(fieldName));
		}
		return 0;
	}

	@Override
	public float getFloat(String fieldName, float defaultValue) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getFloatValueFrom(this.getString(fieldName));
		}
		return defaultValue;
	}

	@Override
	public long getLong(String fieldName) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getLongValueFrom(this.getString(fieldName));
		}
		return 0;
	}

	@Override
	public long getLong(String fieldName, long defaultValue) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getLongValueFrom(this.getString(fieldName));
		}
		return defaultValue;
	}

	@Override
	public double getDouble(String fieldName) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getDoubleValueFrom(this.getString(fieldName));
		}
		return 0;
	}

	@Override
	public double getDouble(String fieldName, double defaultValue) {
		if (this.variableExists(fieldName)) {
			return PrimitiveTypeUtils.getDoubleValueFrom(this.getString(fieldName));
		}
		return defaultValue;
	}

	@Override
	public String getString(String fieldName) {
		return this.request.getParameter(fieldName);
	}

	@Override
	public String getString(String fieldName, String defaultValue) {
		if (this.variableExists(fieldName)) {
			this.getString(fieldName);
		}
		return defaultValue;
	}

	@Override
	public PuObject getPuObject(String fieldName) {
		return PuObject.fromJSON(this.getString(fieldName));
	}

	@Override
	public PuObject getPuObject(String fieldName, PuObject defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getPuObject(fieldName);
		}
		return defaultValue;
	}

	@Override
	public PuArray getPuArray(String fieldName) {
		return PuArrayList.fromJSON(this.getString(fieldName));
	}

	@Override
	public PuArray getPuArray(String fieldName, PuArray defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getPuArray(fieldName);
		}
		return defaultValue;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return "{*** PuHttpRequestObject: " + this.toJSON() + " ***}";
	}

	@Override
	public byte[] getRaw(String fieldName, byte[] defaultValue) {
		return this.variableExists(fieldName) ? this.getRaw(fieldName) : defaultValue;
	}

	@Override
	public PuValue valueOf(String fieldName) {
		throw new OperationNotSupported();
	}
}
