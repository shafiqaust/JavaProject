module.exports = function(grunt) {

	// Configure main project settings
	grunt.initConfig({

		// Basic settings and info about our plugins
		pkg: grunt.file.readJSON('package.json'),
                
		// Minify JS
		uglify: {
			my_target: {
			  files: [{
				expand: true,
				cwd: 'js',
				src: '*.js',
				ext: '.min.js',
				dest: 'js/minified'
			  }]
			}
		},
		
		// Sass
		sass: {
			dist: {
				files: {
					'css/import.css': ['scss/import.scss']
				}
			}
		},
               
		// Minify CSS
		cssmin: {
			combine: {
				files: {
					'css/import.min.css': ['css/import.css']
				}
			}
		},

		// Copy (This will avoid icon fonts url problems with grunt)
		copy: {
		  main: {
				expand: true,
				cwd: 'node_modules/font-awesome/fonts',
				src: '**',
				dest: 'fonts/',
				files: [
			    {
						expand: true,
						flatten: true,
						src: ['node_modules/font-awesome/fonts/*'],
						dest: 'fonts/',
						filter: 'isFile'
					}
				]
		  }
		},
		
		// Watch
        watch: {
			src: {
			  files: ['scss/*.scss','css/*.css','js/*.js'],
			  tasks: ['sass','cssmin','uglify']
			},
			grunt: {
			  files: ['gruntFile.js']
			}
        },
		
	});

	// Load the plugin
	grunt.loadNpmTasks('grunt-contrib-sass');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-cssmin');
	grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-watch');

	// Do the task
	grunt.registerTask('default', ['watch','copy']);

};
