angular.module('lvr')
    .value('identity', {
        id: window.bootstrapData.user.id || 0,
        email: window.bootstrapData.user.id || "",
        idRoot: window.bootstrapData.user.id || 0
    });
