/*
 * Copyright 2019 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.igor.gcb;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ObjectMapper does not properly handle deserializing Google Cloud Build objects such as a Build.
 * In order to work around this, use the Google Cloud recommended parser when we need to deserialize
 * these objects. This class encapsulates that parser so that we can localize the workaround to one
 * place.
 */
@Component
@ConditionalOnProperty("gcb.enabled")
final class GoogleCloudBuildParser {
  private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

  <T> T parse(String input, Class<T> destinationClass) {
    try {
      return gsonFactory.createJsonParser(input).parse(destinationClass);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  <T> T convert(Object input, Class<T> destinationClass) {
    String inputString = serialize(input);
    return parse(inputString, destinationClass);
  }

  String serialize(Object input) {
    Writer writer = new StringWriter();
    try (JsonGenerator generator = gsonFactory.createJsonGenerator(writer)) {
      generator.serialize(input);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return writer.toString();
  }
}
