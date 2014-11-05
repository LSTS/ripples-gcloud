package pt.lsts.ripples.model;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonUtils {

	private static Gson gson = null;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");
	static {
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static synchronized Gson getGsonInstance() {
		if (gson == null) {
			JsonSerializer<Date> ser = new JsonSerializer<Date>() {
				@Override
				public JsonElement serialize(Date src, Type typeOfSrc,
						JsonSerializationContext context) {
					return src == null ? null
							: new JsonPrimitive(dateFormat.format(src));
				}
			};
			gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
					.registerTypeAdapter(Date.class, ser).create();
		}

		return gson;
	}

}
