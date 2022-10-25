/*
    Juego que muestra
    fizz si el núermo es divisible entre 3 
    buzz si el núermo es divisible entre 5
    FIZZBUZZ! si eñ múmero es divisible entre 3 y 5
*/

for (let index = 1; index < 101; index++) {
  
    if(index % 15 === 0){
        console.log(`${index} BIZZBUZZ!`);
    }else if (index % 3 === 0){
        console.log(`${index} Fizz`);
    } else if (index % 5 === 0 ){
        console.log(`${index} Buzz`);
    }
}