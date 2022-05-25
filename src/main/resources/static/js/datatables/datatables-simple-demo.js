window.addEventListener('DOMContentLoaded', event => {
    // Simple-DataTables
    // https://github.com/fiduswriter/Simple-DataTables/wiki

    const individualSummaryTable = document.getElementById('individualSummaryTable');
    if (individualSummaryTable) {
        new simpleDatatables.DataTable(individualSummaryTable);
    }

    const teamSummaryTable = document.getElementById('teamSummaryTable');
    if (teamSummaryTable) {
        new simpleDatatables.DataTable(teamSummaryTable);
    }

    const athleteStatisticsSummaryTable = document.getElementById('athleteStatisticsSummaryTable');
    if (athleteStatisticsSummaryTable) {
        new simpleDatatables.DataTable(athleteStatisticsSummaryTable);
    }

});
