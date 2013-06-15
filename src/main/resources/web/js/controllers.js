'use strict';

/* Controllers */


	function PlaylistCtrl($scope, $filter ) {
        $scope.tracks = [];
        $scope.trackcount = 0;
        $scope.since;
        var pubnub = PUBNUB.init({ subscribe_key : 'sub-c-506c4834-d0ee-11e2-9456-02ee2ddab7fe' });
        pubnub.subscribe({
            channel : "music",                        // CONNECT TO THIS CHANNEL.
            message : function( message, env, channel ) {
            	var track = message;
                $scope.trackcount = track.trackNo;
                $scope.since = track.since;
                //console.log('message recieved '+ received_msg)
                $scope.tracks.unshift(track);
                $scope.$apply()
            }, // RECEIVED A MESSAGE.
            error   : function(data) { console.log(data) }  // CONNECTION BACK ONLINE!
        })
        
        $scope.convertDate = function (date) {
            return $filter('date')(Date.parse(date + '+02:00'), 'HH:mm:ss');
        };  
	}

