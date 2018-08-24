import React, { Component } from 'react'
import styled from 'styled-components'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import TimeAgo from 'react-timeago'
import Leaderboard from '../../components/Leaderboard';

const Wrapper = styled.div`
  display: flex;
  height: 100%;
`

const Column1 = styled.div`
  width: 300px;
  height: 100%;
  text-align: left;
  background-color: hsl(203, 70%, 10%);
  color: hsl(203, 50%, 60%);
  padding: 10px 5px;
`

const CListWrapper = styled.div`
  margin-bottom: 10px;

  &:not(:first-child) {
    margin-top: 30px;
  }
`

const CListHeader = styled.div`
  font-size: 16px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 1px solid hsl(203, 90%, 30%);
  margin-bottom: 10px;
`

const CRecordIcon = styled.span`
  padding: 2px 5px;
  color: ${props => props.wonBy ? 'hsl(120, 50%, 50%)' : 
    (props.completed ? 'hsl(203, 20%, 30%)' : 'hsl(203, 90%, 50%)')};
`

const CRecordWrapper = styled.div`
  color: ${props => props.completed ? 'hsl(203, 20%, 30%)':'hsl(203, 90%, 50%)'};
  display: flex;
  flex-direction: row;
  align-items: center;
  border-radius: 3px;
  cursor: pointer;

  &:hover {
    background-color: hsl(203, 90%, 5%);
  }
`

const CRecordRemaining = styled.span`
  font-size: 12px;
  padding-right: 3px;
`

function timeFormatter(value, unit, suffix) {
  const sf = suffix === 'ago' ? 'ago' : 'left';
  if (unit.startsWith('mon')) {
    return value + 'mo ' + sf;
  } else if (unit.startsWith("sec")) {
    return '< 1min ' + sf;
  } else if (unit.startsWith("m")) {
    return value + ' mins ' + sf;
  } else if (unit.startsWith("h")) {
    return value + 'h ' + sf;
  } else if (unit.startsWith("d")) {
    return value + 'd ' + sf;
  } else if (unit.startsWith("w")) {
    return value + 'w ' + sf;
  } else if (unit.startsWith("y")) {
    return value + 'y ' + sf;
  } else {
    return value + ' ' + unit + ' ' + sf;
  }
}

class ContestRecord extends Component {
  render() {
    const { displayName, endTime, wonBy, completed } = this.props;

    return (
      <CRecordWrapper {...this.props}>
        <CRecordIcon wonBy={wonBy} completed={completed}>
          <FontAwesomeIcon icon="trophy" />
        </CRecordIcon>
        <span style={{flex: 1}}>{displayName}</span>
        <CRecordRemaining><TimeAgo date={endTime} live={false} formatter={timeFormatter} /></CRecordRemaining>
      </CRecordWrapper>
    )
  }
}

class ContestList extends Component {
  render() {
    const { title, contests = [] } = this.props;
    return (
      <CListWrapper>
        <CListHeader>{title}</CListHeader>
        <div>
          {
            contests.map(c => (
              <ContestRecord displayName={c.name} {...c} />
            ))
          }
        </div>
      </CListWrapper>
    )
  }
}

const c1 = [
  {name: 'Best Bug Fixer', completed: false, endTime: new Date(2018, 8, 16)},
  {name: 'Quickest Bug Finder', completed: true, endTime: new Date(2018, 1, 22), wonBy: 23},
  {name: 'Bug #42 Fixer', completed: true, endTime: new Date(2018, 2, 22), wonBy: null},
]

const rks = [
  { name: 'Cersi Lannister', points: (Math.random() * 100000).toFixed(0) },
  { name: 'Jon Snow', points: (Math.random() * 100000).toFixed(0) },
  { name: 'Arya Stark', points: (Math.random() * 100000).toFixed(0) },
  { name: 'Denarys Targerian', points: (Math.random() * 100000).toFixed(0) },
  { name: 'Sansa Stark', points: (Math.random() * 100000).toFixed(0) },
  
]

export default class ContestsPage extends Component {
  render() {
    rks.sort((a,b) => b.points - a.points);
    return (
      <Wrapper>
        <Column1>
          <ContestList title="On Going" contests={c1.filter(c => !c.completed)} />
          <ContestList title="Completed" contests={c1.filter(c => c.completed)} />
        </Column1>
        <div>
          <Leaderboard records={rks} />
        </div>
      </Wrapper>
    )
  }
}
