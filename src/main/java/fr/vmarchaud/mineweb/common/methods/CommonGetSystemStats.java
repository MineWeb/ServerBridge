package fr.vmarchaud.mineweb.common.methods;

import com.google.gson.JsonObject;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.MethodHandler;

@MethodHandler
public class CommonGetSystemStats implements IMethod{

	@Override
	public Object execute(ICore instance, Object... inputs) {
		JsonObject data = new JsonObject();
		data.addProperty("ram", String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000));
		return data;
	}

}
