package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "branches")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchDocument {

    @Id
    private String id;
    private String name;
    private AddressDocument address;

}

