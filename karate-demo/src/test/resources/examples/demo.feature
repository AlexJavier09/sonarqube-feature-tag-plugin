Feature: Demo Karate tests with public API

  @smokeTest
  Scenario: Get a list of users
    Given url 'https://reqres.in/api/users?page=2'
    When method get
    Then status 200
    And match response.page == 2
