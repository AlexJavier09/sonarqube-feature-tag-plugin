Feature: Demo Karate tests with public API

  Scenario: Get a list of users
    Given url 'https://reqres.in/api/users?page=2'
    When method get
    Then status 200
    And match response.page == 2

  Scenario: Create a new user
    Given url 'https://reqres.in/api/users'
    And request { name: 'morpheus', job: 'leader' }
    When method post
    Then status 201
    And match response.name == 'morpheus'

  Scenario: Update a user
    Given url 'https://reqres.in/api/users/2'
    And request { name: 'morpheus', job: 'zion resident' }
    When method put
    Then status 200
    And match response.job == 'zion resident'

  Scenario: Delete a user
    Given url 'https://reqres.in/api/users/2'
    When method delete
    Then status 204
