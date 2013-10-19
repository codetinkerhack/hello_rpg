// Game scene
// -------------
// Runs the core gameplay loop
Crafty.scene('Game', function() {

	// Send move messages on direction key press events
	this.sendMoveOnKeys = Crafty.bind(function(e) {
        if(e.key == Crafty.keys['LEFT_ARROW']) {
              userMove = {name: 'Ev', x: Crafty('PlayerCharacter').x, y: Crafty('PlayerCharacter').y};
              window.ws.send(JSON.stringify(userMove));
        } else if (e.key == Crafty.keys['RIGHT_ARROW']) {
              userMove = {name: 'Ev', x: Crafty('PlayerCharacter').x, y: Crafty('PlayerCharacter').y};
              window.ws.send(JSON.stringify(userMove));
            
        } else if (e.key == Crafty.keys['UP_ARROW']) {
              userMove = {name: 'Ev', x: Crafty('PlayerCharacter').x, y: Crafty('PlayerCharacter').y};
              window.ws.send(JSON.stringify(userMove));
        
        } else if (e.key == Crafty.keys['DOWN_ARROW']) {
              userMove = {name: 'Ev', x: Crafty('PlayerCharacter').x, y: Crafty('PlayerCharacter').y};
              window.ws.send(JSON.stringify(userMove));
        
        }
	});

    
    // Show the victory screen once all villages are visisted
	this.show_victory = this.bind('VillageVisited', function() {
		if (!Crafty('Village').length) {
			Crafty.scene('Victory');
		}
	});
    
}, function() {
	// Remove our event binding from above so that we don't
	//  end up having multiple redundant event watchers after
	//  multiple restarts of the game
	this.unbind('VillageVisited', this.show_victory);
});


// Victory scene
// -------------
// Tells the player when they've won and lets them start a new game
Crafty.scene('Victory', function() {
	// Display some text in celebration of the victory
	Crafty.e('2D, DOM, Text')
		.text('All villages visited!')
		.attr({ x: 0, y: Game.height()/2 - 24, w: Game.width() })
		.css($text_css);

	// Give'em a round of applause!
	Crafty.audio.play('applause');

	// After a short delay, watch for the player to press a key, then restart
	// the game when a key is pressed
	var delay = true;
	setTimeout(function() { delay = false; }, 5000);
	this.restart_game = Crafty.bind('KeyDown', function() {
		if (!delay) {
			Crafty.scene('Game');
		}
	});
}, function() {
	// Remove our event binding from above so that we don't
	//  end up having multiple redundant event watchers after
	//  multiple restarts of the game
	this.unbind('KeyDown', this.restart_game);
});


// Loading scene
// -------------
Crafty.scene('SceneTransition', function(){ 
    
    Crafty.e('2D, DOM, Text')
    	.text('Loading Scene; please wait...')
		.attr({ x: 0, y: Game.height()/2 - 24, w: Game.width() })
		.css($text_css);
    
    
    
    $.ajax({
        url: "/getScene",
        dataType: "json",
      //  context: container,
        success: function(data) {
           
           // Place a tree at every edge square on our grid of 16x16 tiles
            for (var x = 0; x < Game.map_grid.width; x++) {
            	for (var y = 0; y < Game.map_grid.height; y++) {
        			switch (data[x][y]) {
                        case '1': // Tree
                            Crafty.e('Tree').at(x, y);
                            break;
                        case '2': 
                            Crafty.e('Bush').at(x, y);
                            break;
                        case '3': 
                            Crafty.e('Rock').at(x, y);
                            break;
                        case '4': 
                            Crafty.e('Village').at(x, y);
                            break;
                        default: 
        			}
        		}
        	}
        
        },
        
        error: function(jqXHR, textStatus, error) {
            Crafty.e('2D, DOM, Text')
		        .text('Oops.. error loading Scene...')
		        .attr({ x: 0, y: Game.height()/2 - 24, w: Game.width() })
		        .css($text_css);
            
         //   console.log("Error: " + JSON.parse(jqXHR.responseText).error);
            
        }
    });
    
    // Player character, placed at 5, 5 on our grid
	this.player = Crafty.e('PlayerCharacter').at(5, 5);
	//this.occupied[this.player.at().x][this.player.at().y] = true;
    

	// Play a ringing sound to indicate the start of the journey
	Crafty.audio.play('ring');
    
    Crafty.scene('Game');
    
});

// Handles the loading of binary assets such as images and audio files
Crafty.scene('Loading', function(){
	// Draw some text for the player to see in case the file
	//  takes a noticeable amount of time to load
	Crafty.e('2D, DOM, Text')
		.text('Loading; please wait...')
		.attr({ x: 0, y: Game.height()/2 - 24, w: Game.width() })
		.css($text_css);

	// Load our sprite map image
	Crafty.load([
		'assets/images/16x16_forest_2.gif',
		'assets/images/hunter.png',
		'assets/images/door_knock_3x.mp3',
		'assets/images/door_knock_3x.ogg',
		'assets/images/door_knock_3x.aac',
		'assets/images/board_room_applause.mp3',
		'assets/images/board_room_applause.ogg',
		'assets/images/board_room_applause.aac',
		'assets/images/candy_dish_lid.mp3',
		'assets/images/candy_dish_lid.ogg',
		'assets/images/candy_dish_lid.aac'
		], function(){
		// Once the images are loaded...

		// Define the individual sprites in the image
		// Each one (spr_tree, etc.) becomes a component
		// These components' names are prefixed with "spr_"
		//  to remind us that they simply cause the entity
		//  to be drawn with a certain sprite
		Crafty.sprite(16, 'assets/images/16x16_forest_2.gif', {
			spr_tree:    [0, 0],
			spr_bush:    [1, 0],
			spr_village: [0, 1],
			spr_rock:    [1, 1]
		});

		// Define the PC's sprite to be the first sprite in the third row of the
		//  animation sprite map
		Crafty.sprite(16, 'assets/images/hunter.png', {
			spr_player:  [0, 2]
		}, 0, 2);

		// Define our sounds for later use
		Crafty.audio.add({
			knock: 	  ['assets/door_knock_3x.mp3', 'assets/door_knock_3x.ogg', 'assets/door_knock_3x.aac'],
			applause: ['assets/board_room_applause.mp3', 'assets/board_room_applause.ogg', 'assets/board_room_applause.aac'],
			ring:     ['assets/candy_dish_lid.mp3', 'assets/candy_dish_lid.ogg', 'assets/candy_dish_lid.aac']
		});

		// Now that our sprites are ready to draw, start the game
		Crafty.scene('SceneTransition');
	})
});