// App wrapper for the bundled, MIT-licensed min2phase.js solver.
// Keep this separate so the upstream solver remains unmodified.
var qubeScrambler = (function () {
    var trivialStates = Object.create(null);
    var solved = 'UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB';
    trivialStates[solved] = true;
    'URFDLB'.split('').forEach(function (face) {
        ['', '2', "'"].forEach(function (suffix) {
            trivialStates[min2phase.fromScramble(face + suffix)] = true;
        });
    });

    function generate() {
        var state;
        do {
            state = min2phase.randomCube();
        } while (trivialStates[state]);

        var scramble = new min2phase.Search().solution(
            state, 21, 1000000000, 0, min2phase.INVERSE_SOLUTION
        ).trim().replace(/\s+/g, ' ');
        if (!/^[URFDLB](?:2|')?(?: [URFDLB](?:2|')?)*$/.test(scramble)) {
            throw new Error('Could not generate a valid 3x3 scramble: ' + scramble);
        }
        // Verify the inverse solution really creates the sampled state.
        if (min2phase.fromScramble(scramble) !== state) {
            throw new Error('Generated scramble does not match the sampled cube state.');
        }
        return scramble;
    }

    min2phase.initFull();
    return { generate: generate };
})();
