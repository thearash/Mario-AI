initial			= hub
				;

# low and high refer to the tension
# low-tension zones are relaxed
# high-tension zones are frenzied

hub				= low hub
				| high hub
				;
				
low				= lelem
				| lelem low , 3
				;

high			= helem
				| helem high , 2
				;
				
go_up			= GROUND_UP
				;
				
go_down			= GROUND_DOWN
				;
				
lin				= go_up , 3
				| go_down , 1
				;
				
lelem			= go_up coins_blocks go_down , 2
				| patternprob , 3
				| lin , 4
				;
				
helem			= go_down zigzag_blocks FLAT go_up , 4
				| pipe rows pipe, 2
				| cannon , 0.5
				| patternprob , 4
				| lin , 4
				;	
				
rows			= zigzag_blocks , 2
				| coins_blocks  , 2
				| spares , 4
				;
				
enemy		 	= GOOMBA , 6
				| GREENTURTLE , 6
				| REDTURTLE , 6
				;
				
coins			= COINS
				| COINS coins , 2
				;			
				
block			= BLOCK_EE , 5
				| BLOCK_CC
				| BLOCK_CE , 5
				| BLOCK_PE , 5
				;

blocks			= block
				| block blocks , 4
				;
				
coins_blocks	= blocks_enemies , 0.5
				| coins
				;
				
block_enemy		= GOOMBA_BLOCK_EE , 5
				| GOOMBA_BLOCK_CE , 5
				| GOOMBA_BLOCK_CC , 5
				| GREENTURTLE_BLOCK_EE , 5
				| GREENTURTLE_BLOCK_CC , 5
				| GREENTURTLE_BLOCK_CE , 5
				| REDTURTLE_BLOCK_PE , 0.3
				;
				
blocks_enemies	= block_enemy
				| block_enemy blocks_enemies , 0.7
				| FLAT blocks_enemies , 0.5
				;
				
enemy_block		= BLOCK_EE_GOOMBA
				| BLOCK_CE_GOOMBA , 2
				| BLOCK_CC_GOOMBA , 2
				| BLOCK_PE_GOOMBA , 2
				;
				
gapgap			= GAP GAP
				| GAP gapgap , 0.01
				; 
				
spares			= spare
				| spare spares , 5
				| rand , 1
				;
				
rand			= pipe
				| STAIRS_UP
				| STAIRS_DOWN
				| FLAT
				| enemy
				| COINS
				;
				
wingedenemy		= GOOMBA_WINGED , 2
				| REDTURTLE_WINGED , 2
				;
				
patternprob		= stairvalley , 0.7
				| pipevalley , 0.7
				| gdvalley , 0.7
				| gapenemy , 0.7
				| pilargap , 0.7
				| horde , 0.7
				;
				
gapenemy		= GAP block_enemy , 0.2
				| GAP wingedenemy , 0.2
				;
				
pilargap		= GAP PIPE GAP
				| pilargap , 0.1
				;				
				
stairvalley		= STAIRS_UP FLAT sparec FLAT STAIRS_DOWN
				;
				
pipevalley		= PIPE FLAT sparec FLAT PIPE
				;
				
gdvalley		= GROUND_DOWN FLAT sparec FLAT GROUND_UP
				;
				
horde			= enemy enemy , 3
				| enemy enemy enemy , 0.2
				| enemy enemy enemy enemy , 0.2
				;				
				
spare			= block_enemy
				| enemy_block
				| block
				| GAP , 3
				| gapgap , 0.3
				| COINS
				| enemy
				| wingedenemy , 1
				| GAP , 0.5
				| pipe , 0.3
				| FLAT , 0.4
				| rand , 1
				;
				
sparec			= block_enemy
				| enemy_block
				| block
				| GAP , 3
				| gapgap , 0.3
				| enemy
				| wingedenemy , 1
				| GAP , 0.5
				| pipe , 0.3
				| FLAT , 0.4
				;
				
zigzag_blocks	= all_block FLAT 
				| all_block zigzag_blocks , 4
				;
				
all_block		= enemy_block , 3
				| block_enemy , 3
				| block
				| FLAT , 0.5
				| GAP , 0.5
				;
			
pipe			= PIPE , 3
				| PIPE_PIRANHA , 3
				;
				
cannon			= FLAT rows FLAT CANNON
				;	