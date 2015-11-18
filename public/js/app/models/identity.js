angular.module('lvr')
    .value('identity', {
        id: (window.bootstrapData && window.bootstrapData.user) ? window.bootstrapData.user.id : 0,
        email: (window.bootstrapData && window.bootstrapData.user) ? window.bootstrapData.user.email : "",
        idRoot: (window.bootstrapData && window.bootstrapData.user) ? window.bootstrapData.user.idRoot : 0
    });
