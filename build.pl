
use strict;

#$ENV{PATH} = "C:\j2sdk1.4.2_03\bin;".$ENV{PATH};

my $packagedir = "uk/me/alexhaig/FooCompile";
my $appdir = ".";
my $defapp = "FooCompile";

my $buildtarget = shift;

die "Can't find package directory $packagedir" unless (-d $packagedir);
die "Can't find app directory $appdir" unless (-d $appdir);

opendir(PACKAGE, $packagedir);
opendir(APP, $appdir);

my @packagefiles = ();
my @appfiles = ();
my @tokens = ();
my @nodes = ();
my @other = ();

while ($_ = readdir(PACKAGE)) {
    if (/^(.*).java$/) {
        my $name = $1;
        push @packagefiles, $name;
        if ($name =~ /^Token[A-Z]/) {
            push @tokens, $name;
        }
        elsif ($name =~ /^Node[A-Z]/) {
            push @nodes, $name;
        }
        else {
            push @other, $name;
        }
    }
}

while ($_ = readdir(APP)) {
    if (/^(.*).java$/) {
        push @appfiles, $1;
    }
}

open MAKEFILE, ">temp.mak";

printmakespecial("default", ["$defapp.class"], []);
printmakespecial("all", makeappnames(\@appfiles, "class"), []);

foreach (@appfiles) {
    printmakething("$_", makepackagenames(\@other, "class"), [ makeappcmd($_) ]);
}

foreach (@other) {
    my @deps;
    if ($_ eq 'Tokeniser') {
        my $tokenpkg = makepackagenames(\@tokens, "class"); 
        push @deps, @$tokenpkg;
    }
    if ($_ eq 'Lexer') {
        my $nodepkg = makepackagenames(\@nodes, "class"); 
        push @deps, @$nodepkg;
    }
    printmakething("$packagedir/$_", \@deps, [makepkgcmd($_)]);
}

foreach (@tokens) {
    printmakething("$packagedir/$_", [ "$packagedir/Token.class" ], [makepkgcmd($_)]);
}

foreach (@nodes) {
    printmakething("$packagedir/$_", [ "$packagedir/Node.class" ], [makepkgcmd($_)]);
}

close MAKEFILE;

#system("nmake /nologo /f temp.mak $buildtarget") && die "Failed to run make";
system("make -f temp.mak $buildtarget") && die "Failed to run make";

exit();

sub printmakething($$$) {
    my ($target, $deps, $actions) = @_;
    $actions = [ $actions ] unless ref $actions;

    print MAKEFILE "$target.class: $target.java ".join(' ',@$deps)."\n";
    foreach (@$actions) {
        print MAKEFILE "\t$_\n";
    }
    print MAKEFILE "\n";
}

sub printmakespecial($$$) {
    my ($target, $deps, $actions) = @_;
    $actions = [ $actions ] unless ref $actions;

    print MAKEFILE "$target: ".join(' ',@$deps)."\n";
    foreach (@$actions) {
        print MAKEFILE "\t$_\n";
    }
    print MAKEFILE "\n";
}


sub makepackagenames($$) {
    my ($arr, $extn) = @_;
    $arr = [$arr] unless ref $arr;
    my @ret;
    foreach (@$arr) {
        push @ret, "$packagedir/$_.$extn"; 
    }
    return \@ret;
}

sub makeappnames($$) {
    my ($arr, $extn) = @_;
    $arr = [$arr] unless ref $arr;
    my @ret;
    foreach (@$arr) {
        push @ret, "$_.$extn"; 
    }
    return \@ret;
}

sub makeappcmd($) {
    my $thing = shift;
    return "javac $thing.java";
}
sub makepkgcmd($) {
    my $thing = shift;
    return "javac $packagedir/$thing.java";
}

