# OpenXES CLI

This is a command line interface for the OpenXES library. It allows you to convert XES files to CSV files and vice
versa.

## Usage

Example:

```shell
java -jar build/libs/openxes-cli.jar -f input.xes -t csv -o outut.csv
```

CLI options:

```
usage: openxes-cli
 -f,--from <arg>     Input file path
 -h,--help           Print help message
 -o,--output <arg>   Output file path (optional)
 -t,--to <arg>       Output format extension
```