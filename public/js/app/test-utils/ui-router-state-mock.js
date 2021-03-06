/**
 * Copyright: https://gist.github.com/geraldofcneto/7d4690dc8c81b0f1fde0
 * ui-router always request default state's template (even if $state is not used in tests)
 * this fixes the problem
 */
angular.module('stateMock', []);
angular.module('stateMock').service("$state", function($q) {
    this.expectedTransitions = [];
    this.transitionTo = function(stateName, params) {
        if (this.expectedTransitions.length > 0) {
            var expectedState = this.expectedTransitions.shift();
            if (expectedState.stateName !== stateName) {
                throw Error('Expected transition to state: ' + expectedState.stateName + ' but transitioned to ' + stateName);
            }
            if (!angular.equals(expectedState.params, params)) {
                throw Error('Expected params to be ' + JSON.stringify(expectedState.params) + ' but received ' + JSON.stringify(params));
            }
        } else {
            throw Error("No more transitions were expected! Tried to transition to " + stateName);
        }

        return $q.when();
    };
    this.go = this.transitionTo;
    this.expectTransitionTo = function(stateName, params) {
        this.expectedTransitions.push({stateName: stateName, params: params});
    };

    this.ensureAllTransitionsHappened = function() {
        if (this.expectedTransitions.length > 0) {
            throw Error("Not all transitions happened!");
        }
    };
});