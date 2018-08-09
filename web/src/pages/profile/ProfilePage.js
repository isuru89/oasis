import React, { Component } from 'react'
import styled from 'styled-components'
import Avatar from '../../components/Avatar'
import profileImg from '../../profile.jpg'
import Panel from '../../components/Panel';
import ImageTitleValue, {ImageContent} from '../../components/ImageTitleValue';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import PointLabel from '../../components/PointLabel';
import BadgeView from '../../components/BadgeView';
import TeamHistoryRecord from './TeamHistoryRecord';
import RankView from './RankView';
import RankPoint from './RankPoint';
import MilestoneView from "./MilestoneView";
import PointBreakdown from './PointBreakdown';

const Content = styled.div`
  height: 100%;
`

const UserContent = styled.div`
  display: flex;
  width: 100%;
  height: 100%;
`

const Column1 = styled.div`
  width: 300px;
  text-align: center;
  background-color: #1E2B34;
  padding: 10px 5px;
`

const Column2 = styled.div`
  flex: 5;
`

const Column3 = styled.div`
  flex: 3;
`

const UserNameTitle = styled.div`
  margin-top: 10px;
  text-transform: uppercase;
  font-size: 24px;
  letter-spacing: 1.5px;
  font-weight: 600;
  line-height: 40px;
`

const UserDesignation = styled.div`
  border-bottom: 3px solid #ffffff33;
  line-height: 14px;
  padding-bottom: 10px;
  color: #ddd;
  font-size: 12px;
  letter-spacing: 1.7px;
  font-weight: 400;
`

const leaderboardData = [
  { leaderboard: 'Top Supporter', points: (Math.random() * 100000).toFixed(0), rank: 2 },
  { leaderboard: 'Top Finder', points: (Math.random() * 500).toFixed(0), rank: 15 },
  { leaderboard: 'Top Closer', points: (Math.random() * 500).toFixed(0), rank: (Math.random() * 20).toFixed(0) },
]

const milestoneData = [
  { milestone: 'Total Resolves', level: 4, totalLevels: 10, remaining: (Math.random() * 10000).toFixed(0), nextLevel: 10000, progress: 55 },
  { milestone: 'Quick Resolves', level: 2, totalLevels: 5, remaining: (Math.random() * 10000).toFixed(0), nextLevel: 10000, progress: 32 },
  { milestone: 'Bugs Found', level: 9, totalLevels: 20, remaining: (Math.random() * 10000).toFixed(0), nextLevel: 10000, progress: 81 },
];

const pointBreakData = { 
    total: (Math.random() * 100000).toFixed(2),
    records: [
      { label: 'Ticket Resolves', points: (Math.random() * 50000).toFixed(2) },
      { label: 'Violation Fixes', points: (Math.random() * 40000).toFixed(2) },
      { label: 'Bonus', points: (Math.random() * 20000).toFixed(2) },
      { label: 'Awards', points: (Math.random() * 10000).toFixed(2) },
    ]
  };


export default class ProfilePage extends Component {
  render() {
    return (
      <Content>
        <UserContent>
          <Column1>
            <Avatar image={profileImg} size={164} />
            <UserNameTitle>John Doe</UserNameTitle>
            <UserDesignation>Senior Engineer</UserDesignation>
            <ImageTitleValue image={<FontAwesomeIcon icon="at" />}
              title="john@product.com" />

            <ImageTitleValue image={<FontAwesomeIcon icon="football-ball" />}
              title="Team:" 
              value="QA-Testings" />
            
            <RankView data={leaderboardData} />

          </Column1>
          <Column2>
            <Panel title="Milestone Progress">
              <MilestoneView data={milestoneData} />
            </Panel>

            <UserContent>
              <Column2>
                <Panel title="MY BADGES (5)">
                  <div><BadgeView /></div>
                </Panel>
              </Column2>
              <Column3>
                <Panel title="TEAM HISTORY">
                  <TeamHistoryRecord team="Team-1" active={true} />
                  <TeamHistoryRecord />
                  <TeamHistoryRecord />
                </Panel>
                <Panel title="Point Breakdown">
                  <PointBreakdown {...pointBreakData} />
                </Panel>
              </Column3>
              
            </UserContent>
          
          </Column2>
        </UserContent>
      </Content>
    )
  }
}
