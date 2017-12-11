/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.management.internal.cli.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.apache.geode.test.junit.categories.UnitTest;
import org.apache.geode.test.junit.rules.GfshParserRule;


@Category(UnitTest.class)
public class CreateGatewaySenderCommandTest {
  @ClassRule
  public static GfshParserRule gfsh = new GfshParserRule();

  private CreateGatewaySenderCommand command;

  @Before
  public void before() {
    command = spy(CreateGatewaySenderCommand.class);
  }

  @Test
  public void missingId() {
    gfsh.executeAndAssertThat(command, "create gateway-sender --remote-distributed-system-id=1")
        .statusIsError().containsOutput("Invalid command");
  }

  @Test
  public void missingRemoteId() {
    gfsh.executeAndAssertThat(command, "create gateway-sender --id=ln").statusIsError()
        .containsOutput("Invalid command");
  }

  @Test
  public void missingOrderPolicy() {
    gfsh.executeAndAssertThat(command,
        "create gateway-sender --id=ln --remote-distributed-system-id=1 "
            + "--dispatcher-threads=2")
        .statusIsError()
        .containsOutput("Must specify --order-policy when --dispatcher-threads is larger than 1");

    doReturn(Collections.EMPTY_SET).when(command).findMembers(any(), any());
    gfsh.executeAndAssertThat(command,
        "create gateway-sender --id=ln --remote-distributed-system-id=1 "
            + "--dispatcher-threads=1")
        .statusIsError().containsOutput("No Members Found");
  }

  @Test
  public void paralleAndThreadOrderPolicy() {
    gfsh.executeAndAssertThat(command,
        "create gateway-sender --id=ln --remote-distributed-system-id=1 "
            + "--parallel --order-policy=THREAD")
        .statusIsError()
        .containsOutput("Parallel Gateway Sender can not be created with THREAD OrderPolicy");
  }

  @Test
  public void orderPolicyAutoComplete() {
    String command =
        "create gateway-sender --id=ln --remote-distributed-system-id=1 --order-policy";
    GfshParserRule.CommandCandidate candidate = gfsh.complete(command);
    assertThat(candidate.getCandidates()).hasSize(3);
    assertThat(candidate.getFirstCandidate()).isEqualTo(command + "=KEY");
  }
}