package it.gov.pagopa.authorizer.config.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

@Container(containerName = "skeydomains")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class SubscriptionKeyDomain implements Serializable {

  @Id
  @GeneratedValue
  @JsonProperty("id")
  private String id;

  @PartitionKey
  @JsonProperty("domain")
  private String domain;

  @JsonProperty("subscription_key")
  private String subscriptionKey;

  @JsonProperty("description")
  private String description;

  @JsonProperty("owner_id")
  private String ownerId;

  @JsonProperty("owner_name")
  private String ownerName;

  @JsonProperty("owner_type")
  private String ownerType;

  @JsonProperty("authorized_entities")
  private List<AuthorizedEntity> authorizedEntities;

  @JsonProperty("other_metadata")
  private List<Metadata> otherMetadata;

  @JsonProperty("inserted_at")
  private String insertedAt;

  @JsonProperty("last_forced_refresh")
  private String lastForcedRefresh;

  @JsonProperty("_ts")
  private String lastUpdate;

  /*
  "authorized_entities": [
    {
      "name": "Ente Y",
      "value": "entity1"
    },
    {
      "name": "Ente Z",
      "values": ["entity-section-1", "entity-section-2", "entity-section-N"]
    },
  ],
  "other_metadata": [
    {
      "name": "metadata_name",
      "short_key": "_key",
      "content": [
        {
          "key": "key1",
          "value": "value1"
        },
        {
          "key": "key2",
          "values": ["value1", "value2", "valueN"]
        }
      ]
    }
  ],
  "inserted_at": "2023-01-01T00:00:00.000Z",
  "last_forced_refresh": "2023-01-01T00:00:00.000Z",
  "_ts": "2023-01-01T00:00:00.000Z"
   */
}
