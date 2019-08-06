package com.coreoz.plume.admin.services.logApi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

import com.coreoz.plume.admin.db.generated.LogHeader;
import com.google.common.collect.ImmutableList;

public class LogHeaderServiceTest {

	@Test
	public void check_that_json_content_type_can_be_detected() {
		Optional<MimeType> mimeType = LogHeaderService.guessResponseMimeType(ImmutableList.of(
			header("Date", "Tue, 06 Aug 2019 09:51:53 GMT"),
			header("Content-Type", "application/json; odata.metadata=minimal"),
			header("Expires", "-1")
		));

		assertThat(mimeType).isPresent();
		assertThat(mimeType.get()).isSameAs(MimeType.JSON);
	}

	@Test
	public void check_that_xml_content_type_can_be_detected() {
		Optional<MimeType> mimeType = LogHeaderService.guessResponseMimeType(ImmutableList.of(
			header("date", "Tue, 06 Aug 2019 09:51:53 GMT"),
			header("content-type", "application/xml; odata.metadata=minimal"),
			header("expires", "-1")
		));

		assertThat(mimeType).isPresent();
		assertThat(mimeType.get()).isSameAs(MimeType.XML);
	}

	@Test
	public void check_that_no_content_type_returns_an_empty_result() {
		Optional<MimeType> mimeType = LogHeaderService.guessResponseMimeType(ImmutableList.of());
		assertThat(mimeType).isEmpty();
	}

	@Test
	public void check_that_an_unknown_content_type_returns_an_empty_result() {
		Optional<MimeType> mimeType = LogHeaderService.guessResponseMimeType(ImmutableList.of(
			header("content-type", "unkown content type")
		));
		assertThat(mimeType).isEmpty();
	}

	static LogHeader header(String name, String value) {
		LogHeader header = new LogHeader();
		header.setName(name);
		header.setValue(value);
		return header;
	}

}
