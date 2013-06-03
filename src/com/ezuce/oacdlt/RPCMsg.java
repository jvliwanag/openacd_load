package com.ezuce.oacdlt;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class RPCMsg {
	private String id;
	private String method;
	
	private JSONArray params;
	
	@SuppressWarnings("unchecked")
	public RPCMsg(String id, String method, Object... os) {
		this.id = id;
		this.method = method;

		this.params = new JSONArray();
		this.params.addAll(Arrays.asList(os));
	}
	
	public JSONObject toJSON() {
		Map<Object, Object> kv = new HashMap<>();

		kv.put("id", id);
		kv.put("method", method);
		kv.put("params", params);
		kv.put("jsonrpc", "2.0");
		
		JSONObject obj = new JSONObject(kv);
		return obj;
	}
	
	public String toString() {
		return toJSON().toJSONString();
	}
}
