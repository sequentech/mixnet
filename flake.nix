{
  description = "A simple flake for building and installing the mixnet.";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-parts.url = "github:hercules-ci/flake-parts";
  };
  outputs = inputs@{ flake-parts, ... }:
    flake-parts.lib.mkFlake { inherit inputs; } {
      imports = [];
      systems = [ "x86_64-linux" ];

      # Per-system attributes can be defined here. The self' and inputs'
      # module parameters provide easy access to attributes of the same
      # system.
      perSystem = perSystemInputs@{ config, self', inputs', pkgs, system, lib, ... }:
        let
          mixnetModule = (import ./mixnet-package.nix);
          mixnet = mixnetModule (perSystemInputs);
        in {
          packages = {
            inherit mixnetModule;
            inherit mixnet;
            default = mixnet;
          };
        };
        flake = {};
    };
}
