package codegenerator;

import java.io.File;

public class Config {

	// Can change
	public static final String SRC_FOLDER = "src/main/java/";
	public static final String SRC_TEST_FOLDER = "src/test/java/";
	public static final String APP_PKG = "com.company";
	public static final String BASE_REPOSITORY_FULLNAME = APP_PKG + ".bases.BaseRepository";
	public static final String BASE_REPOSITORY_NAME = "BaseRepository";
	public static final String ENDPOINTS_PREFIX = "/api/";

	// Can NOT change without customize templates
	public static final String TEMPLATES = "src/templates/";
	public static final String SRC_DIR = SRC_FOLDER.replace("/", File.separator);
	public static final String REPOSITORY_DIR = String.format("%s%s.repository", SRC_DIR, APP_PKG).replace(".", File.separator);
	public static final String SERVICE_DIR = String.format("%s%s.service", SRC_DIR, APP_PKG).replace(".", File.separator);
	public static final String CONTROLLER_DIR = String.format("%s%s.controller", SRC_DIR, APP_PKG).replace(".", File.separator);
	public static final String DTO_REQ_DIR = String.format("%s%s.dto.req", SRC_DIR, APP_PKG).replace(".", File.separator);
	public static final String DTO_RES_DIR = String.format("%s%s.dto.res", SRC_DIR, APP_PKG).replace(".", File.separator);

	public static final String SRC_TEST_DIR = SRC_TEST_FOLDER.replace("/", File.separator);
	public static final String SERVICE_TEST_DIR = String.format("%s%s.service", SRC_TEST_DIR, APP_PKG).replace(".", File.separator);
}
