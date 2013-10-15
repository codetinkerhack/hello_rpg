        var gameLoop = function() {
            // the configurations
            var config = {
                forwardKey: 38, // go ahead
                backwardKey: 40,
                turnLeftKey: 37,
                turnRightKey: 39
            };

            var canvas = document.getElementById('canvas'),
                context = canvas.getContext('2d');

            var heroFront = utils.preloadImage('hero-front.png');
            var heroBack = utils.preloadImage('hero-back.jpg');
			var heroRight = utils.preloadImage('hero-left.jpg');
			var heroLeft = utils.preloadImage('hero-right.jpg');
           // var heavyCHeroTurret = utils.preloadImage('heavy-ct-turret.png');
            var rocket = utils.preloadImage('rocket.png');

            var Hero = {
                body: heroFront,
              //  turret: heavyCHeroTurret,
                weapon: rocket,
                firing: [], // the rockets or bullets on firing
                onFire: false, // It's firing
                x: 0,
                y: 0,
                degreesPerTurn: 90,
                turretDegreesPerTurn: 8, // if change degreePerTurn and turretDegreesPer, need to change turn function
                config: {
                    speed: 5
                },
                speed: 0, // this Hero's speed
                rocketSpeed: 20,
                cooldown: 300, // in million second
                firingPrepared: true, // if true, can fire, decided by cooldown time.
                direction: 0, // 1-12 clock-wise
                turretDirection: 0,
                move: function(direction) { // forward or backward, need to specific by the direction. F/B
                    var speed = this.speed;
                    if (direction === 'B') { 
						this.body = heroBack;
						this.y -= this.config.speed;
                        
                        userMove = {name: 'Ev', x: this.x, y: this.y};
                        window.ws.send(JSON.stringify(userMove));
					
                    } else
                    if (direction === 'F') { 
						this.body = heroFront;
						this.y += this.config.speed;
                        
                        userMove = {name: 'Ev', x: this.x, y: this.y};
                        window.ws.send(JSON.stringify(userMove));

                    }
					else
                    if (direction === 'L') { 
						this.body = heroLeft;
						this.x -= this.config.speed;
                        
                        userMove = {name: 'Ev', x: this.x, y: this.y};
                        window.ws.send(JSON.stringify(userMove));
                    
                    }
					else
                    if (direction === 'R') { 
						this.body = heroRight;
						this.x += this.config.speed;
                    
                        userMove = {name: 'Ev', x: this.x, y: this.y};
                        window.ws.send(JSON.stringify(userMove));

                    }

                    //this.x += speed * Math.sin(this.direction * this.degreesPerTurn * Math.PI / 180);
                     // * Math.cos(this.direction * this.degreesPerTurn * Math.PI / 180);

                    // firing
                    if (this.onFire) {
                        for (var i = 0; i < this.firing.length; i++) {
                            this.firing[i].x += this.rocketSpeed * Math.sin(this.firing[i].direction * this.turretDegreesPerTurn * Math.PI / 180);
                            this.firing[i].y -= this.rocketSpeed * Math.cos(this.firing[i].direction * this.turretDegreesPerTurn * Math.PI / 180);
                        }       
                    }
                },
                turn: function(direction) { // turn left or turn right, 30 degrees per keydown. L/R
                    switch(direction) {
                        case 'L':
							this.body = heroLeft;
                            this.direction--;
							
                            //this.turretDirection -= 2.5; // 2.5 cause 10/4 (degreesPerTurn/turretDegreesPerTurn
                            break;
                        case 'R':
							this.body = heroRight;
                            this.direction++;
							
                            //this.turretDirection += 2.5;
                            break;
                    }
                }
                /*turnTurret: function(direction) { // turn the turret
                    switch(direction) {
                        case 'L':
                            this.turretDirection--;
                            break;
                        case 'R':
                            this.turretDirection++;
                            break;
                    }
                },
                fire: function() {
                    this.onFire = true;
                    if (this.firingPrepared) { // can fire
                        var rocketStartX = this.x + 25 * Math.sin(this.turretDirection * this.turretDegreesPerTurn * Math.PI / 180),
                        rocketStartY = this.y - 25 * Math.cos(this.turretDirection * this.turretDegreesPerTurn * Math.PI / 180);
                        this.firing.push({
                            x: rocketStartX,
                            y: rocketStartY,
                            direction: this.turretDirection
                        });
                        this.firingPrepared = false;
                        var thisHero = this;
                        function preparingFire() {
                            thisHero.firingPrepared = true;
                        }
                        window.setTimeout(preparingFire, thisHero.cooldown);
                        console.log(this.firingPrepared);
                    }
                }*/
            };

            function drawHero(hero) {
                var width = hero.body.width;
                var height = hero.body.height;
                //var turretWidth = tank.turret.width;
                //var turretHeight = tank.turret.height;
                //var angleInRadians = tank.direction * tank.degreesPerTurn * Math.PI / 180;
                //var turretAngleInRadians = tank.turretDirection * tank.turretDegreesPerTurn * Math.PI / 180;

                
                context.translate(hero.x, hero.y);
               // context.rotate(angleInRadians);
                //context.drawImage(tank.body, tank.x, tank.y);
                context.drawImage(hero.body, -width / 2, - height / 2, width, height);
                //context.rotate(-angleInRadians);
                context.translate(-hero.x, -hero.y);

                // rotate turret
                /*context.translate(tank.x, tank.y);
                context.rotate(turretAngleInRadians);
                context.drawImage(tank.turret, -turretWidth / 2 -1, -turretHeight / 2 -5, turretWidth, turretHeight); 
                context.rotate(-turretAngleInRadians);
                context.translate(-tank.x, -tank.y);
*/
                // fire, draw rocket
                if (Hero.onFire) {
                    for (var i = 0; i < hero.firing.length; i++) {
                        var rocketAngleInRadians = hero.firing[i].direction * hero.turretDegreesPerTurn * Math.PI / 180;
                        var rocketWidth = hero.weapon.width,
                        rocketHeight = hero.weapon.height;

                        context.translate(hero.firing[i].x, hero.firing[i].y);
                        context.rotate(rocketAngleInRadians);
                        context.drawImage(hero.weapon, rocketWidth / 2 -5, rocketHeight / 2 -2, rocketWidth, rocketHeight);
                        context.rotate(-rocketAngleInRadians);
                        context.translate(-hero.firing[i].x, -hero.firing[i].y);
                    }
                }

            }


            // Initical the tank, add the eventListener to the window.
            var init = function() {
                /*function onControl(event) {
                    switch (event.keyCode) {
                        // forward 38
                        case 38:
                        event.preventDefault();
                        tank.move('F');
                        break;
                        // backward 40
                        case 40:
                        event.preventDefault();
                        tank.move('B');
                        break;
                        // turn left
                        case 37:
                        event.preventDefault();
                        tank.turn('L');
                        break;
                        // turn right
                        case 39:
                        event.preventDefault();
                        tank.turn('R');
                        break;
                    }
                }*/
                function onControlKeydown (event) {
                    switch (event.keyCode) {
                        // forward 38
                        case 38:
                            event.preventDefault();
							Hero.move('B');
                            break;
                        // backward 40
                        case 40:
                            event.preventDefault();
							Hero.move('F');
                            break;
                        // turn left
                        case 37:
                            event.preventDefault();
							Hero.move('L');
                            break;
                        // turn right
                        case 39:
                            event.preventDefault();
							Hero.move('R');
                            break;
                        // fire
                        case 32:
                            event.preventDefault();
                            Hero.fire();
                            break;
                    }
                }
                function onControlKeyup (event) {
                    switch (event.keyCode) {
                        // forward 38
                        case 38:
                        event.preventDefault();
                        //tank.speed = 0;
                        break;
                        // backward 40
                        case 40:
                        event.preventDefault();
                        //tank.speed = 0;
                        break;
                        // turn left
                        case 37:
                        event.preventDefault();
                        //tank.turn('L');
                        break;
                        // turn right
                        case 39:
                        event.preventDefault();
                        //tank.turn('R');
                        break;
                    }
                }

                // control the turret by mouse
                var lastX = 0;
                function onControlMousemove (event) {
                    if (event.pageX < lastX) { // move left
                        Hero.turretDirection--;
                    } else {
                        Hero.turretDirection++;
                    }
                    lastX = event.pageX;
                }

                // left click for fire
                function onClick(event) {
                    hero.fire();
                }
                window.addEventListener('keydown', onControlKeydown, false);
                window.addEventListener('keyup', onControlKeyup, false);
                canvas.addEventListener('mousemove', onControlMousemove, false);
                canvas.addEventListener('click', onClick, false);
            }
            init();
           (function drawFrame() {
                window.requestAnimationFrame(drawFrame, canvas);
                context.clearRect(0, 0, canvas.width, canvas.height);
                Hero.move();
                drawHero(Hero);
            }());
        };
