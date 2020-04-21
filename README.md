## The GOS Language

GOS was originally created to somewhat simplify creating Source engine configs.

##### Why "GOS"?

It got its name by a concatenation of Global Offensive and Script. 
Undeniably CS:GO is one of the most popular games ever written in Source. CSS sounds like CS Source, CSGOS is too long, and SS sounds like the German elite forces, so I chose GOS.

##### Why use it?

It greatly simplifies and clarifies scripting

##### Examples

I though it would be prudent to present the examples in a form of GOS vs ordinary Source CFG file.

Example 1:

    bind "key" {
        echo "pressed"
    }

vs 

    bind key "echo pressed"
    
Example 2:

    bind "key" {
        echo "two words"
    }
    
vs 

    bind key "exec cfg"
    
cfg.cfg:

    echo "two words"
    
Example 3:

    bind "key" + {
        echo "pressed"
    } - {
        echo "released"
    }
    
vs

    alias +mybind "echo pressed"
    alias -mybind "echo released"
    bind key +mybind
    
Example 4:

    bind "key" # {
        echo "1"
    }, {
        echo "2"
    }, {
        echo "3"
    }
    
vs 

    alias _bind1 "echo 1; alias bind _bind2"
    alias _bind2 "echo 2; alias bind _bind3"
    alias _bind3 "echo 3; alias bind _bind1"
    alias mybind _bind1
    bind key mybind

##### Usage

You have to have Java installed to run GOSC (GOS Compiler).

To compile a script `scriptPath/script.gos` to `outPath` under name `out` using GOSC located in `GOSCPath/gosc.jar` using Java stored under `javaPath` you have to execute this:

    "javaPath/java" -jar "GOSCPath/gosc.jar" "scriptPath/script.gos" "outPath" out
    
The program then creates a `out.cfg` in `outPath` containing the main scripts, and creates a folder named `__gencfg_out`, where additional scripts are situated (some features would be impossible without them).
    
##### Warnings / syntax features

All arguments should be put in quotes:

    bind "p" "+mybind"
    sv_cheats "1"
    ent_fire "!player" "addoutput" "maxhealth 1000"


This syntax feature makes programs like this valid:

    ent_fire
        "!player"
        "addoutput"
        "maxhealth 1000"
        
It adds to readability.

    
Only the alias statement accepts an identifier as he first argument and a scope as the second argument.

    alias mybind1 { echo "kar" }
    alias mybind2 + { echo "+" } - { echo "-" }
    alias mybind3 # { echo "1" }, { echo "2" }, { echo "3" }
    
Realiases cannot be done like this:

    alias yourbind mybind
    
You should do:

    alias yourbind1 { mybind1 }
    alias yourbind2 + { +mybind2 } - { -mybind2 }
    alias yourbind3 { mybind3 }

Be warned that the invocation of yourbind3 will also influence the order of mybind3.
To avoid this you will have to copy the alias:

    alias yourbind3 # { clause1 }, { clause2 }, ...

Semicolons are not required:

    echo "1" echo "2"

They will be ignored.

This program

    bind "p" { +mybind }
    
will not function properly, you'll have to do 

    bind "p" "+mybind"

or

    bind "p" + { +mybind } - { -mybind }

Comments are created using stars:

    echo "smth" * comment *
    
There are no single-line comments and the comments are not transferred to the compiled output.
