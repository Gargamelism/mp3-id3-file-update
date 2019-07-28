# Mp3 - Id3 - file update

Console app to update mp3 file names based on its id3v2 and vice versa

## Description
If you have a large music library, keeping it in order is tough.
This tool allows to update file names based on the id3v2 details.

## Usage
### Updating a folder example:
```java -jar mp3-editor-0.0.1-standalone.jar -f -p ".\Finntroll\2013 - Blodsvept"```

### Updating specific files example
```java -jar mp3-editor-0.0.1-standalone.jar -f -n ".\Finntroll\2010 - Nifelvind\(06) [Finntroll] Tiden Utan Tid.mp3"```

### Available flags (can be viewed if run without flags)
```
  -f, --file                     rename files
  -t, --tag                      update id3 tag
  -p, --path PATH                folder with desired files
  -n, --file-names FILE_NAMES    comma delimeted list of single files to update
  -F, --file-name-format FORMAT  new file name using %ARTIST%, %ALBUM%, %TITLE%, %TRACK. Default is "%ARTIST% - %ALBUM% - %TITLE% - %TRACK%"
  -d, --dry-run                  only print the planned changes
  -r, --recursive                change files in all subfolders
```
