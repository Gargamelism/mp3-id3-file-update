# Mp3 - Id3 - file update

Console app to update mp3 file names based on its id3v2 and vice versa

## Description
If you have a large music library, keeping it in order is tough.
This tool allows to update file names based on the id3v2 details.

## Usage
### Updating a folder example:
#### Updating filenames
```java -jar mp3-editor-0.0.1-standalone.jar -f -p ".\Finntroll\2013 - Blodsvept"```
#### Updating tags
```java -jar mp3-editor-0.0.1-standalone.jar -t -p ".\Finntroll\2013 - Blodsvept" -F "(%TRACK%) [%ARTIST%] %TITLE%" -A Blodsvept```

### Updating specific files example
#### Updating filenames
```java -jar mp3-editor-0.0.1-standalone.jar -f -n ".\Finntroll\2010 - Nifelvind\(06) [Finntroll] Tiden Utan Tid.mp3,.\Finntroll\2010 - Nifelvind\(06) [Finntroll] Solsagan.mp3"```
#### Updating tags
```java -jar mp3-editor-0.0.1-standalone.jar -t -n ".\Finntroll\2010 - Nifelvind\(06) [Finntroll] Tiden Utan Tid.mp3,.\Finntroll\2010 - Nifelvind\(06) [Finntroll] Solsagan.mp3" -F "(%TRACK%) [%ARTIST%] %TITLE%" -A Nifelvind```

## Formatting notes
* ".mp3" will be added by the code.
* If there are sections you wish to ignore but are not consistent you can use *%IGNORE%*, for example:
..```... -n ".\Finntroll\2010 - Nifelvind\(06) zOrO [Finntroll] Tiden Utan Tid.mp3, .\Finntroll\2010 - Nifelvind\(06) d4ryL [Finntroll] Tiden Utan Tid.mp3 -F "(%TRACK%) %IGNORE% [%ARTIST%] %TRACK%"

### Available flags (can be viewed if run without flags)
```
  -f, --file-name                rename files
  -t, --tag                      update id3 tag
  -p, --path PATH                folder with desired files
  -n, --file-names FILE_NAMES    comma delimeted list of single files to update
  -F, --file-name-format FORMAT  format for file renaming or info retrieval from files (to fill id3) %ARTIST%, %ALBUM%, %TITLE%, %TRACK. Default is "%ARTIST% - %ALBUM% - %TRACK% - %TITLE%"
  -a, --artist ARTIST            track's artist, shouldn't be used recursively unless root is band's dir - can be used with format
  -A, --album ALBUM              track's album, shouldn't be used recursively - can be used with format
  -d, --dry-run                  only print the planned changes
  -r, --recursive                change files in all subfolders
```
