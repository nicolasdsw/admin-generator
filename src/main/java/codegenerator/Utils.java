package codegenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Utils {

	public static String generateEndpoint(String modelName) {
		return Config.ENDPOINTS_PREFIX
				+ (modelName.endsWith("y") ? modelName.substring(0, modelName.length() - 2) + "ies" : modelName + "s")
						.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase();
	}

	public static void createPkg(String pkg) {
		Path path = Paths.get(pkg);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Scans all classes accessible from the context class loader which belong to
	 * the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = (URL) resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	public static List<Class<?>> getExternalClasses(String dir, String packageName) throws Exception {
		File file = new File(dir);
		// convert the file to URL format
		URL url = file.toURI().toURL();
		URL[] urls = new URL[] { url };
		// load this folder into Class loader
		@SuppressWarnings("resource")
		ClassLoader classLoader = new URLClassLoader(urls);

		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = (URL) resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base
	 *                    directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	public static boolean isPrimitiveEnumOrWrapper(Class<?> clazz) {
		return clazz.isPrimitive() || clazz.isEnum() || clazz.equals(String.class) || clazz.equals(Boolean.class)
				|| clazz.equals(Integer.class) || clazz.equals(Character.class) || clazz.equals(Byte.class)
				|| clazz.equals(Short.class) || clazz.equals(Double.class) || clazz.equals(Long.class)
				|| clazz.equals(Float.class);
	}

	public static boolean isDate(Class<?> clazz) {
		return clazz.equals(LocalDate.class) || clazz.equals(LocalTime.class) || clazz.equals(LocalDateTime.class);
	}

	public static String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		char baseChar = str.charAt(0);
		char updatedChar;
		updatedChar = Character.toUpperCase(baseChar);
		if (baseChar == updatedChar) {
			return str;
		}
		char[] chars = str.toCharArray();
		chars[0] = updatedChar;
		return new String(chars, 0, chars.length);
	}
}
