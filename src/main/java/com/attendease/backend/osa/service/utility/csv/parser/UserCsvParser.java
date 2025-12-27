package com.attendease.backend.osa.service.utility.csv.parser;

import com.attendease.backend.domain.user.account.management.users.csv.row.UserAccountManagementUsersCSVRowData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserCsvParser {

	private static final Set<String> REQUIRED_COLUMNS = Set.of("firstname", "lastname", "studentnumber", "password");

	public static List<UserAccountManagementUsersCSVRowData> parse(InputStream inputStream) throws IOException {
		try (CSVParser parser = CSVParser.parse(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8),
				CSVFormat.Builder.create()
						.setHeader()
						.setSkipHeaderRecord(true)
						.setTrim(true)
						.setIgnoreEmptyLines(true).get()
		)) {

			validateHeader(parser);

			Map<String, String> headerMap = parser.getHeaderNames().stream()
					.collect(Collectors.toMap(String::toLowerCase, h -> h, (a, b) -> a));

			List<UserAccountManagementUsersCSVRowData> rows = new ArrayList<>();

			for (CSVRecord record : parser) {
				rows.add(parseRow(record, headerMap));
			}

			return rows;
		}
	}

	private static void validateHeader(CSVParser parser) {
		Set<String> headers = parser.getHeaderNames().stream().map(String::toLowerCase).collect(Collectors.toSet());
		List<String> missing = REQUIRED_COLUMNS.stream().filter(r -> !headers.contains(r)).toList();

		if (!missing.isEmpty()) {
			throw new IllegalArgumentException(
					"Missing required columns: " + String.join(", ", missing)
			);
		}
	}

	private static UserAccountManagementUsersCSVRowData parseRow(CSVRecord record, Map<String, String> headers) {
		UserAccountManagementUsersCSVRowData data = new UserAccountManagementUsersCSVRowData();
		data.setFirstName(get(record, headers, "firstname"));
		data.setLastName(get(record, headers, "lastname"));
		data.setStudentNumber(get(record, headers, "studentnumber"));
		data.setPassword(get(record, headers, "password"));
		data.setEmail(get(record, headers, "email"));
		data.setSectionName(get(record, headers, "section"));
		data.setContactNumber(get(record, headers, "contactnumber"));
		return data;
	}

	private static String get(CSVRecord record, Map<String, String> headers, String key) {
		String header = headers.get(key);
		if (header == null) return null;

		String value = record.get(header);
		return (value == null || value.isBlank()) ? null : value.trim();
	}
}

