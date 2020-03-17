package codegenerator;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

	public static void main(String[] args) throws Exception {
		List<Class<?>> modelClasses = Utils.getExternalClasses("C://Users//nicolas.souza//git//admin-generator//",
				Config.APP_PKG + ".model");
		Utils.createPkg(Config.REPOSITORY_DIR);
		Utils.createPkg(Config.SERVICE_DIR);
		Utils.createPkg(Config.DTO_REQ_DIR);
		Utils.createPkg(Config.DTO_RES_DIR);
		Utils.createPkg(Config.CONTROLLER_DIR);
		Utils.createPkg(Config.SERVICE_TEST_DIR);
		for (Class<?> clazz : modelClasses) {
			if (!clazz.getName().endsWith("Builder")) {
				buildRepository(clazz);
				buildReqDTO(clazz);
				buildResDTO(clazz);
				buildService(clazz);
				buildController(clazz);
				buildServiceTest(clazz);
			}
		}
	}

	public static void buildRepository(Class<?> clazz) throws Exception {
		String modelName = clazz.getSimpleName();
		String idType = clazz.getDeclaredField("id").getType().getSimpleName();
		try {
			String dir = Config.TEMPLATES + "template_repository.txt".replace("/", File.separator);
			String template = Files.lines(Paths.get(dir)).collect(Collectors.joining(System.lineSeparator()));
			template += System.lineSeparator();
			template = template.replace("{app_pkg}", Config.APP_PKG)
					.replace("{baserepository_pkg}", Config.BASE_REPOSITORY_FULLNAME)
					.replace("{baserepository_name}", Config.BASE_REPOSITORY_NAME).replace("{model_name}", modelName)
					.replace("{id_type}", idType);
			String fileName = String.format("%s%s%sRepository.java", Config.REPOSITORY_DIR, File.separator, modelName);
			Path newFile = Files.createFile(Paths.get(fileName));
			Files.write(newFile, template.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println(modelName + ": " + e.getMessage());
		}
	}

	public static void buildReqDTO(Class<?> clazz) throws Exception {
		String modelName = clazz.getSimpleName();
		String imports = "";
		String fields = "";
		List<String> importsList = new ArrayList<>();
		String converters = "";

		for (Field field : clazz.getDeclaredFields()) {
			if ("serialVersionUID".equals(field.getName()))
				continue;
			if (Collection.class.isAssignableFrom(field.getType()))
				continue;
			if (field.getName().equals("id"))
				continue;

			String fieldNameCap = Utils.capitalize(field.getName());

			for (Annotation annotation : field.getAnnotations()) {
				if (annotation.annotationType().getName().contains("javax.validation.constraints")) {
					fields += "	@" + annotation.annotationType().getSimpleName() + System.lineSeparator();
					String annotationImport = "import " + annotation.annotationType().getName() + ";"
							+ System.lineSeparator();
					if (!importsList.contains(annotationImport)) {
						importsList.add(annotationImport);
						imports += annotationImport;
					}
				}
			}

			if (Utils.isPrimitiveEnumOrWrapper(field.getType())) {
				fields += String.format("	private %s %s;", field.getType().getSimpleName(), field.getName());

				converters += String.format("		entity.set%s(this.%s);", fieldNameCap, field.getName());
			} else if (Utils.isDate(field.getType())) {
				fields += String.format("	private %s %s;", field.getType().getSimpleName(), field.getName());
				String importLine = "import " + field.getType().getName() + ";" + System.lineSeparator();
				if (!importsList.contains(importLine)) {
					importsList.add(importLine);
					imports += importLine;
				}
				converters += String.format("		entity.set%s(this.%s);", fieldNameCap, field.getName());
			} else {
				fields += String.format("	private %s %sId;",
						field.getType().getDeclaredField("id").getType().getSimpleName(), field.getName());

				String importLine = "import " + field.getType().getName() + ";" + System.lineSeparator();
				if (!importsList.contains(importLine)) {
					importsList.add(importLine);
					imports += importLine;
				}

				converters += String.format("		entity.set%s(this.%sId == null ? null", fieldNameCap,
						field.getName());
				converters += " : {field_type}.builder().id(this.{field_name}Id).build());"
						.replace("{field_name}", field.getName())
						.replace("{field_type}", clazz.getDeclaredField(field.getName()).getType().getSimpleName());
			}

			fields += System.lineSeparator();
			converters += System.lineSeparator();
		}
		if (!importsList.isEmpty()) {
			imports = System.lineSeparator() + imports;
		}
		try {
			String dir = Config.TEMPLATES + "template_dto_request.txt".replace("/", File.separator);
			String template = Files.lines(Paths.get(dir)).collect(Collectors.joining(System.lineSeparator()));
			template += System.lineSeparator();
			template = template.replace("{app_pkg}", Config.APP_PKG).replace("{model_name}", modelName)
					.replace("{imports}", imports).replace("{fields}", fields).replace("{converters}", converters);
			String fileName = String.format("%s%s%sReqDTO.java", Config.DTO_REQ_DIR, File.separator, modelName);
			Path newFile = Files.createFile(Paths.get(fileName));
			Files.write(newFile, template.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println(modelName + ": " + e.getMessage());
		}
	}

	public static void buildResDTO(Class<?> clazz) throws Exception {
		String modelName = clazz.getSimpleName();
		String idType = clazz.getDeclaredField("id").getType().getSimpleName();
		String imports = "";
		String fields = "";
		List<String> importsList = new ArrayList<>();
		String converters = "";

		if (clazz.getSuperclass() != null) {
			for (Field field : clazz.getSuperclass().getDeclaredFields()) {
				if ("serialVersionUID".equals(field.getName()))
					continue;
				if (Collection.class.isAssignableFrom(field.getType()))
					continue;
				if (field.getName().equals("id"))
					continue;

				if (Utils.isPrimitiveEnumOrWrapper(field.getType())) {
					fields += String.format("	private %s %s;", field.getType().getSimpleName(), field.getName());
				} else if (Utils.isDate(field.getType())) {
					fields += String.format("	private %s %s;", field.getType().getSimpleName(), field.getName());
					String importLine = "import " + field.getType().getName() + ";" + System.lineSeparator();
					if (!importsList.contains(importLine)) {
						importsList.add(importLine);
						imports += importLine;
					}
				}
				fields += System.lineSeparator();

				converters += String.format("		this.%s = entity.get%s();", field.getName(),
						Utils.capitalize(field.getName()));
				converters += System.lineSeparator();
			}
		}

		for (Field field : clazz.getDeclaredFields()) {
			if ("serialVersionUID".equals(field.getName()))
				continue;
			if (Collection.class.isAssignableFrom(field.getType()))
				continue;
			if (field.getName().equals("id"))
				continue;

			String fieldNameCap = Utils.capitalize(field.getName());

			if (Utils.isPrimitiveEnumOrWrapper(field.getType())) {
				fields += String.format("	private %s %s;", field.getType().getSimpleName(), field.getName());

				converters += String.format("		this.%s = entity.get%s();", field.getName(), fieldNameCap);
			} else if (Utils.isDate(field.getType())) {
				fields += String.format("	private %s %s;", field.getType().getSimpleName(), field.getName());
				String importLine = "import " + field.getType().getName() + ";" + System.lineSeparator();
				if (!importsList.contains(importLine)) {
					importsList.add(importLine);
					imports += importLine;
				}
				converters += String.format("		this.%s = entity.get%s();", field.getName(), fieldNameCap);
			} else {
				fields += String.format("	private %s %sId;",
						field.getType().getDeclaredField("id").getType().getSimpleName(), field.getName());
				fields += System.lineSeparator();
				fields += String.format("	private String %sDesc;", field.getName());

				converters += String.format("		if (entity.get%s() != null) {", fieldNameCap)
						+ System.lineSeparator();
				converters += String.format("			this.%sId = entity.get%s().getId();", field.getName(),
						fieldNameCap) + System.lineSeparator();
				converters += String.format("			this.%sDesc = entity.get%s().toString();", field.getName(),
						fieldNameCap) + System.lineSeparator();
				converters += "		}";
			}

			converters += System.lineSeparator();
			fields += System.lineSeparator();
		}
		if (!importsList.isEmpty()) {
			imports = System.lineSeparator() + imports;
		}
		try {
			String dir = Config.TEMPLATES + "template_dto_response.txt".replace("/", File.separator);
			String template = Files.lines(Paths.get(dir)).collect(Collectors.joining(System.lineSeparator()));
			template += System.lineSeparator();
			template = template.replace("{app_pkg}", Config.APP_PKG).replace("{model_name}", modelName)
					.replace("{imports}", imports).replace("{fields}", fields).replace("{id_type}", idType)
					.replace("{converters}", converters);
			String fileName = String.format("%s%s%sResDTO.java", Config.DTO_RES_DIR, File.separator, modelName);
			Path newFile = Files.createFile(Paths.get(fileName));
			Files.write(newFile, template.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println(modelName + ": " + e.getMessage());
		}
	}

	public static void buildService(Class<?> clazz) throws Exception {
		String modelName = clazz.getSimpleName();
		String idType = clazz.getDeclaredField("id").getType().getSimpleName();
		String columns = "";
		for (Field field : clazz.getDeclaredFields()) {
			if (Utils.isPrimitiveEnumOrWrapper(field.getType()) && !Modifier.isStatic(field.getModifiers())) {
				columns += String.format(", \"%s\"", field.getName());
			}
		}
		try {
			String dir = Config.TEMPLATES + "template_service.txt".replace("/", File.separator);
			String template = Files.lines(Paths.get(dir)).collect(Collectors.joining(System.lineSeparator()))
					+ System.lineSeparator();
			template = template.replace("{app_pkg}", Config.APP_PKG).replace("{model_name}", modelName)
					.replace("{id_type}", idType).replace("{columns}", columns);
			String fileName = String.format("%s%s%sService.java", Config.SERVICE_DIR, File.separator, modelName);
			Path newFile = Files.createFile(Paths.get(fileName));
			Files.write(newFile, template.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println(modelName + ": " + e.getMessage());
		}
	}

	public static void buildController(Class<?> clazz) throws Exception {
		String modelName = clazz.getSimpleName();
		String idType = clazz.getDeclaredField("id").getType().getSimpleName();
		try {
			String dir = Config.TEMPLATES + "template_controller.txt".replace("/", File.separator);
			String template = Files.lines(Paths.get(dir)).collect(Collectors.joining(System.lineSeparator()))
					+ System.lineSeparator();
			template = template.replace("{app_pkg}", Config.APP_PKG).replace("{model_name}", modelName)
					.replace("{endpoint}", Utils.generateEndpoint(modelName)).replace("{id_type}", idType);
			String fileName = String.format("%s%s%sController.java", Config.CONTROLLER_DIR, File.separator, modelName);
			Path newFile = Files.createFile(Paths.get(fileName));
			Files.write(newFile, template.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println(modelName + ": " + e.getMessage());
		}
	}

	public static void buildServiceTest(Class<?> clazz) throws Exception {
		String modelName = clazz.getSimpleName();
		String idType = clazz.getDeclaredField("id").getType().getSimpleName();
		try {
			String dir = Config.TEMPLATES + "template_test_service.txt".replace("/", File.separator);
			String template = Files.lines(Paths.get(dir)).collect(Collectors.joining(System.lineSeparator()))
					+ System.lineSeparator();
			template = template.replace("{app_pkg}", Config.APP_PKG).replace("{model_name}", modelName)
					.replace("{id_type}", idType);
			String fileName = String.format("%s%s%sServiceTest.java", Config.SERVICE_TEST_DIR, File.separator,
					modelName);
			Path newFile = Files.createFile(Paths.get(fileName));
			Files.write(newFile, template.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			System.err.println(modelName + ": " + e.getMessage());
		}
	}
}
