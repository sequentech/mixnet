{ pkgs, config, lib, ... }:

{
  # https://devenv.sh/basics/
  env.GREET = "devenv";

  # https://devenv.sh/packages/
  packages = [
    pkgs.git
    pkgs.ack

    # to create containers
    pkgs.docker

    pkgs.libffi
  ];

  enterShell = ''
    git --version
  '';

  # https://devenv.sh/languages/
  languages.nix.enable = true;
  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk8;
  };
}
