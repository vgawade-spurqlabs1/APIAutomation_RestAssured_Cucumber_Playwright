Feature: All Notes API Validation

  @api

  Scenario Outline: Validate POST Create Notes API Response for "<scenarioName>" Scenario
    When User sends "<method>" request to "<url>" with headers "<headers>" and query file "<queryFile>" and body file "<bodyFile>"
    Then User verifies the response status code is <statusCode>
    And User verifies the response body matches JSON schema "<schemaFile>"
    Then User verifies fields in response: "<contentType>" with content type "<fields>"
    Examples:
      | scenarioName       | method | url                                                             | headers        | queryFile | bodyFile             | statusCode | schemaFile | contentType | fields         |
      | Valid create Notes | POST   | /api/v1/loan-syndications/{dealId}/investors/{investorId}/notes | NA             | NA        | Create_Notes_Request | 200        | NA         | NA          | NA             |

  @api

  Scenario Outline: Validate GET Notes API Response for "<scenarioName>" Scenario
    When User sends "<method>" request to "<url>" with headers "<headers>" and query file "<queryFile>" and body file "<bodyFile>"
    Then User verifies the response status code is <statusCode>
    And User verifies the response body matches JSON schema "<schemaFile>"
    Then User verifies fields in response: "<contentType>" with content type "<fields>"
    Examples:
      | scenarioName    | method | url                                                             | headers        | queryFile | bodyFile | statusCode | schemaFile       | contentType | fields              |
      | Valid Get Notes | GET    | /api/v1/loan-syndications/{dealId}/investors/{investorId}/notes | NA             | NA        | NA       | 200        | Notes_Schema_200 | json        | note=This is Note 1 |

  @api

  Scenario Outline: Validate Update Notes API Response for "<scenarioName>" Scenario
    When User sends "<method>" request to "<url>" with headers "<headers>" and query file "<queryFile>" and body file "<bodyFile>"
    Then User verifies the response status code is <statusCode>
    And User verifies the response body matches JSON schema "<schemaFile>"
    Then User verifies fields in response: "<contentType>" with content type "<fields>"
    Examples:
      | scenarioName       | method | url                                                                                   | headers        | queryFile | bodyFile             | statusCode | schemaFile | contentType | fields         |
      | Valid update Notes | PUT    | /api/v1/loan-syndications/{dealId}/investors/{investorId}/notes/{noteId}/update-notes | NA             | NA        | Update_Notes_Request | 200        | NA         | NA          | NA             |


  Scenario Outline: Validate DELETE Create Notes API Response for "<scenarioName>" Scenario
    When User sends "<method>" request to "<url>" with headers "<headers>" and query file "<queryFile>" and body file "<bodyFile>"
    Then User verifies the response status code is <statusCode>
    And User verifies the response body matches JSON schema "<schemaFile>"
    Then User verifies fields in response: "<contentType>" with content type "<fields>"
    Examples:
      | scenarioName | method | url                                                                      | headers        | queryFile | bodyFile | statusCode | schemaFile | contentType | fields         |
      | Valid delete | DELETE | /api/v1/loan-syndications/{dealId}/investors/{investorId}/notes/{noteId} | NA             | NA        | NA       | 200        | NA         | NA          | NA             |