## CodingManager

\
*CodingManager is a compact CLI for comparing Run-length and Huffman coding - file compression techniques.*

## Setup

---
Latest version of CodingManager is using Java 17: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html \
Set up the ```JAVA_HOME``` path variable after installation, as well as add the downloaded JDK's bin folder to ```PATH``` (if previous versions of JDK are installed).
Alternatively, use the downloaded version's bin folder to run the project directly from the terminal.


## Usage

---
CodingManager accepts certain keys in order to configure the execution process. After running the command, the respective output will be produced in the run directory (mind your surroundings). Two testing files have been provided as a sample; feel free to test (don't forget to uncomment from .gitignore if you wish to store them with the project).

### Run command

```
java -jar CodingManager-jar-with-dependencies.jar 

<help>[optional] 
<encode> <sourceFileName>[for-encode] 
<decode> <huffmanFile>/<runlengthFile>[for-decode]
<debug> [optional]
```


### CLI args

```<help [-h | --help]>``` - provides description of keys for the CLI (flag) \
```<encode [-enc | --encode]>``` - option for encoding the source file with both algorithms (flag) \
```<source [-sf | --source-file]>``` - use with ```[-enc | --encode]``` to provide path to the file to be encoded (string) \
```<decode [-dec | --decode]>``` - option for decoding the huffman, run-length or both files (no flag) \
```<huffman-file [-he | --huffman-encoded]>``` - use with ```[-dec | --decode]``` to decode the Huffman-encoded file for provided path (string) \
```<runlength-file [-rle | --rl-encoded]>``` - use with ```[-dec | --decode]``` to decode the runlength-encoded file for provided path (string) \
```<debug [-D | --debug]>``` - use this option to produce additional output during encoding/decoding process (flag)

\
Examples for both acceptable and forbidden key combinations are provided follow below (in Unix).

### Acceptable

```
-h
-enc -sf /Users/xxx/Desktop/test.txt -D
--encode --source-file /Users/xxx/Desktop/test.txt -D
--decode -he /Users/xxx/Desktop/test-HC.enc -rle /Users/xxx/Desktop/test-RL.enc --debug
```

### Forbidden

```
-h -enc -sf ... --encode --decode
--encode --decode -h
etc.
```
\
**Note: only decode the encoded files produced by this CLI - other software tools will be using other encoding methods.**

### Contact

---
* Telegram:  @nk25.dev
* Email:  nk25.dev@gmail.com
---