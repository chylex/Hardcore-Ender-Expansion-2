Param([Parameter(Mandatory = $True, Position = 1)][string] $filePath)
Add-Type -AssemblyName System.Windows.Forms
$files = new-object System.Collections.Specialized.StringCollection
$files.Add($filePath)
[System.Windows.Forms.Clipboard]::SetFileDropList($files)
