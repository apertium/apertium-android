Feature: Login feature

  Scenario: As a valid user I can log into my app
    When I press "Download languages"
    Then I see "Apertium install language pair"
#    When I press "Esperanto ⇆ English"
    When I press "Swedish → Danish"
    Then I see "Marked to install"
    When I press "Apply"
    Then I see "Downloading"


